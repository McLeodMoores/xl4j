/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.msvc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages allocation of UUIDs for COM interfaces.
 */
public class UUIDManager {

  private final Map<String, String> _interfaces = new HashMap<String, String> ();

  /**
   * Creates an instance with no initial state.
   */
  public UUIDManager () {
  }

  /**
   * Creates an instance using data from a disk store.
   * 
   * @param reader
   *          source of UUID information, previously written by {@link #save}
   */
  public UUIDManager (final Reader reader) throws IOException {
    final BufferedReader br = new BufferedReader (reader);
    String line;
    while ((line = br.readLine ()) != null) {
      line = line.trim ();
      final int eq = line.indexOf ('=');
      if (eq > 0) {
        final String interfaceName = line.substring (0, eq);
        final String uuid = line.substring (eq + 1);
        _interfaces.put (interfaceName, uuid);
      }
    }
    br.close ();
  }

  /**
   * Returns the UUID for an interface.
   * <p>
   * If the interface has been seen before then a UUID will be re-used. Otherwise a new one will be
   * allocated.
   * 
   * @param interfaceName
   *          interface name, not {@code null}
   * @return the UUID string, never {@code null}
   */
  public synchronized String uuidFor (final String interfaceName) {
    String uuidStr = _interfaces.get (interfaceName);
    if (uuidStr == null) {
      final UUID uuid = UUID.randomUUID ();
      uuidStr = uuid.toString ();
      _interfaces.put (interfaceName, uuidStr);
    }
    return uuidStr;
  }

  /**
   * Writes the data from this store to a file that can initialise an instance in the future.
   * 
   * @param writer
   *          destination of UUID information, not {@code null}
   */
  public synchronized void save (final Writer writer) throws IOException {
    final PrintWriter pw = new PrintWriter (writer);
    final List<String> interfaceNames = new ArrayList<String> (_interfaces.keySet ());
    Collections.sort (interfaceNames);
    for (final String interfaceName : interfaceNames) {
      pw.println (interfaceName + "=" + _interfaces.get (interfaceName));
    }
    pw.close ();
  }

}