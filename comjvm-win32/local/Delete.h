/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

HRESULT DeleteFilesAndFoldersA (PCSTR pszPath);
HRESULT DeleteFilesAndFoldersW (PCWSTR pszPath);
#ifdef _UNICODE
# define DeleteFilesAndFolders DeleteFilesAndFoldersW
#else /* ifdef _UNICODE */
# define DeleteFilesAndFolders DeleteFilesAndFoldersA
#endif /* ifdef _UNICODE */
