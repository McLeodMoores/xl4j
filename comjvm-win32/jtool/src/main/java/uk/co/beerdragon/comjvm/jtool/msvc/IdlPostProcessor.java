/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.msvc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.co.beerdragon.comjvm.jtool.post.FileHeader;
import uk.co.beerdragon.comjvm.jtool.post.PostProcessingStrategy;

/**
 * Post processes IDL file content.
 * <p>
 * The file is pre-pended with forward declarations for all interfaces and any {@code import} lines
 * for referenced interfaces from other packages.
 */
public class IdlPostProcessor implements PostProcessingStrategy {

  public static final PostProcessingStrategy HEADER = new FileHeader (Arrays.asList (
      "// Automatically generated file; do not modify", "import \"core.idl\";"));

  private final File _idlFolder;

  /**
   * Creates a new instance.
   * 
   * @param idlFolder
   *          folder whether other IDL files have been written to, not {@code null}
   */
  public IdlPostProcessor (final File idlFolder) {
    _idlFolder = Objects.requireNonNull (idlFolder);
  }

  private static <K extends Comparable<K>> List<Map.Entry<K, Boolean>> sortByKey (
      final Collection<Map.Entry<K, Boolean>> values) {
    final List<Map.Entry<K, Boolean>> result = new ArrayList<Map.Entry<K, Boolean>> (values);
    Collections.sort (result, new Comparator<Map.Entry<K, Boolean>> () {

      @Override
      public int compare (final Entry<K, Boolean> o1, final Entry<K, Boolean> o2) {
        return o1.getKey ().compareTo (o2.getKey ());
      }

    });
    return result;
  }

  /* package */boolean idlExists (final String name) {
    return (new File (_idlFolder, name)).exists ();
  }

  // PostProcessingStrategy

  @Override
  public List<String> postProcess (final List<String> content) {
    final Map<String, Boolean> interfaces = new HashMap<String, Boolean> ();
    final Pattern interfaceDecl = Pattern
        .compile (".*interface\\s+([^\\s]+)\\s+:\\s+([^\\s]+)\\s+\\{");
    final Pattern methodDecl = Pattern.compile ("\\s+HRESULT\\s+[^\\s]+\\s+\\(([^;]+);");
    for (final String line : content) {
      Matcher matcher = interfaceDecl.matcher (line);
      if (matcher.matches ()) {
        interfaces.put (matcher.group (1), Boolean.FALSE);
        if (interfaces.get (matcher.group (2)) == null) {
          interfaces.put (matcher.group (2), Boolean.TRUE);
        }
      } else {
        matcher = methodDecl.matcher (line);
        if (matcher.matches ()) {
          final String args = matcher.group (1);
          int start = 0, tok;
          while ((tok = args.indexOf (']', start)) > 0) {
            if (args.charAt (tok + 2) == 'I') {
              final int spc = args.indexOf ('*', tok + 2);
              final String iface = args.substring (tok + 2, spc);
              if (interfaces.get (iface) == null) {
                interfaces.put (iface, Boolean.TRUE);
              }
              start = spc;
            } else {
              start = tok + 1;
            }
          }
        }
      }
    }
    final Set<String> imports = new HashSet<String> (interfaces.size ());
    final List<String> forwards = new ArrayList<String> (interfaces.size ());
    for (final Map.Entry<String, Boolean> interfaceEntry : sortByKey (interfaces.entrySet ())) {
      String interfaceName = interfaceEntry.getKey ();
      if ("IJObject".equals (interfaceName) || "IJDispatch".equals (interfaceName)
          || "IUnknown".equals (interfaceName)) {
        continue;
      }
      if (interfaceEntry.getValue ()) {
        if (interfaceName.startsWith ("IJ") && Character.isUpperCase (interfaceName.charAt (2))) {
          interfaceName = interfaceName.substring (1);
        }
        int len = interfaceName.length ();
        while (len > 1) {
          final String candidatePackageName = interfaceName.substring (1, len - 1) + "."
              + IdlFileContent.EXT;
          if (idlExists (candidatePackageName)) {
            imports.add (candidatePackageName);
            break;
          }
          len--;
        }
      } else {
        forwards.add ("interface " + interfaceName + ";");
      }
    }
    final List<String> result = new ArrayList<String> (content.size () + imports.size () * 9
        + forwards.size ());
    result.addAll (forwards);
    result.add ("#ifndef __forwards_only__");
    for (final String importFile : imports) {
      result.add ("#ifndef __FWD_" + importFile.replace ('.', '_'));
      result.add ("#define __FWD_" + importFile.replace ('.', '_'));
      result.add ("#define __forwards_only__");
      result.add ("#include \"" + importFile + "\"");
      result.add ("#endif");
    }
    result.addAll (content);
    for (final String importFile : imports) {
      result.add ("#ifndef __INC_" + importFile.replace ('.', '_'));
      result.add ("#define __INC_" + importFile.replace ('.', '_'));
      result.add ("#include \"" + importFile + "\"");
      result.add ("#endif");
    }
    result.add ("#else");
    result.add ("#undef __forwards_only__");
    result.add ("#endif");
    return result;
  }
}
