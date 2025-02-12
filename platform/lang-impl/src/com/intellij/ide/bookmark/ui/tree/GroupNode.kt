// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.ide.bookmark.ui.tree

import com.intellij.icons.AllIcons
import com.intellij.ide.bookmark.Bookmark
import com.intellij.ide.bookmark.BookmarkBundle.message
import com.intellij.ide.bookmark.BookmarkGroup
import com.intellij.ide.bookmark.BookmarkProvider
import com.intellij.ide.bookmark.LineBookmark
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.ide.util.treeView.AbstractTreeNodeCache
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes

internal class GroupNode(project: Project, group: BookmarkGroup) : AbstractTreeNode<BookmarkGroup>(project, group) {
  private val cache = AbstractTreeNodeCache<Bookmark, AbstractTreeNode<*>>(this) { it.createNode() }

  override fun getChildren(): List<AbstractTreeNode<*>> {
    var bookmarks = value.getBookmarks()

    // shows line bookmarks only in the popup
    if (parentRootNode?.value?.isPopup == true) {
      bookmarks = bookmarks.filterIsInstance<LineBookmark>()
    }

    // reuse cached nodes
    var nodes = cache.getNodes(bookmarks).onEach { if (it is BookmarkNode) it.bookmarkGroup = value }
    BookmarkProvider.EP.getExtensions(project).sortedByDescending { it.weight }.forEach { nodes = it.prepareGroup(nodes) }
    return nodes
  }

  override fun update(presentation: PresentationData) {
    presentation.setIcon(AllIcons.Nodes.BookmarkGroup)
    if (value.isDefault) {
      presentation.addText("${value.name}  ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
      presentation.addText(message("default.group.marker"), SimpleTextAttributes.GRAYED_ATTRIBUTES)
    }
    else {
      presentation.presentableText = value.name
    }
  }
}
