/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once

HRESULT DeleteFilesAndFoldersA (PCSTR pszPath);
HRESULT DeleteFilesAndFoldersW (PCWSTR pszPath);
#ifdef _UNICODE
# define DeleteFilesAndFolders DeleteFilesAndFoldersW
#else /* ifdef _UNICODE */
# define DeleteFilesAndFolders DeleteFilesAndFoldersA
#endif /* ifdef _UNICODE */
