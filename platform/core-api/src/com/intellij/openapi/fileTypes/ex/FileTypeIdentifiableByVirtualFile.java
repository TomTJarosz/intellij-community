/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.openapi.fileTypes.ex;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayFactory;
import org.jetbrains.annotations.NotNull;

/**
 * {@link FileType} which is determined by the particular {@link VirtualFile}.
 * For example, text file located in "META-INF/services" directory should be treated as of SPI file type.
 *
 * <p/>As an implementation example, please
 * @see com.intellij.spi.SPIFileType
 */
public interface FileTypeIdentifiableByVirtualFile extends FileType {
  /**
   * @return true if (and only if) this particular file should be treated as belonging to this file type.
   * Please make sure your definition is consistent with all other definitions for this file type.
   * For example, file type should not be associated with the file name pattern (see Settings|Editor|File Types) which is not mentioned in the {@code isMyFileType()} below.
   */
  boolean isMyFileType(@NotNull VirtualFile file);

  FileTypeIdentifiableByVirtualFile[] EMPTY_ARRAY = new FileTypeIdentifiableByVirtualFile[0];
  ArrayFactory<FileTypeIdentifiableByVirtualFile> ARRAY_FACTORY =
    count -> count == 0 ? EMPTY_ARRAY : new FileTypeIdentifiableByVirtualFile[count];
}