// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.idea.compiler

import com.intellij.diagnostic.PluginException
import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.compiler.CompilationStatusListener
import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.compiler.CompilerMessageCategory
import com.intellij.openapi.compiler.CompilerTopics
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.UntraceableException
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.LocalFileSystem
import org.jetbrains.kotlin.config.CompilerRunnerConstants
import org.jetbrains.kotlin.idea.util.application.isUnitTestMode
import org.jetbrains.kotlin.js.JavaScript
import java.io.PrintStream
import java.io.PrintWriter

internal class KotlinCompilerStartupActivity : StartupActivity {
    // Extending PluginException ensures that Exception Analyzer recognizes this as a Kotlin exception
    private class KotlinCompilerException(private val text: String) :
        PluginException("", PluginManager.getPluginByClass(KotlinCompilerStartupActivity::class.java)?.pluginId),
        UntraceableException {
        override fun printStackTrace(s: PrintWriter) {
            s.print(text)
        }

        override fun printStackTrace(s: PrintStream) {
            s.print(text)
        }

        @Synchronized
        override fun fillInStackTrace(): Throwable {
            return this
        }

        override fun getStackTrace(): Array<StackTraceElement> {
            LOG.error("Somebody called getStackTrace() on KotlinCompilerException")
            // Return some stack trace that originates in Kotlin
            return UnsupportedOperationException().stackTrace
        }

        override val message: String
            get() = "<Exception from standalone Kotlin compiler>"

    }

    companion object {
        private val LOG = Logger.getInstance(KotlinCompilerStartupActivity::class.java)

        // Comes from external make
        private const val PREFIX_WITH_COMPILER_NAME =
            CompilerRunnerConstants.KOTLIN_COMPILER_NAME + ": " + CompilerRunnerConstants.INTERNAL_ERROR_PREFIX
        private val FILE_EXTS_WHICH_NEEDS_REFRESH = setOf(JavaScript.DOT_EXTENSION, ".map")
    }

    override fun runActivity(project: Project) {
        project.messageBus.connect().subscribe(CompilerTopics.COMPILATION_STATUS, object : CompilationStatusListener {
            @Suppress("HardCodedStringLiteral")
            override fun compilationFinished(aborted: Boolean, errors: Int, warnings: Int, compileContext: CompileContext) {
                for (error in compileContext.getMessages(CompilerMessageCategory.ERROR)) {
                    val message = error.message
                    if (message.startsWith(CompilerRunnerConstants.INTERNAL_ERROR_PREFIX) || message.startsWith(PREFIX_WITH_COMPILER_NAME)) {
                        LOG.error(KotlinCompilerException(message))
                    }
                }
            }

            override fun fileGenerated(outputRoot: String, relativePath: String) {
                if (isUnitTestMode()) return

                val ext = FileUtilRt.getExtension(relativePath).toLowerCase()
                if (FILE_EXTS_WHICH_NEEDS_REFRESH.contains(ext)) {
                    val outFile = "$outputRoot/$relativePath"
                    val virtualFile = LocalFileSystem.getInstance().findFileByPath(outFile)
                        ?: error("Virtual file not found for generated file path: $outFile")
                    virtualFile.refresh( /*async =*/false,  /*recursive =*/false)
                }
            }
        })
    }
}
