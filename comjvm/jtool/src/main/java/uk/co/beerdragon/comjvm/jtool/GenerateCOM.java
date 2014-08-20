/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;

import uk.co.beerdragon.comjvm.jtool.filename.FileExtensionStrategy;
import uk.co.beerdragon.comjvm.jtool.filename.FilePerClassStrategy;
import uk.co.beerdragon.comjvm.jtool.filename.FilePerPackageStrategy;
import uk.co.beerdragon.comjvm.jtool.msvc.CppFileContent;
import uk.co.beerdragon.comjvm.jtool.msvc.HppFileContent;
import uk.co.beerdragon.comjvm.jtool.msvc.IdlFileContent;
import uk.co.beerdragon.comjvm.jtool.msvc.IdlPostProcessor;
import uk.co.beerdragon.comjvm.jtool.msvc.UUIDManager;
import uk.co.beerdragon.comjvm.jtool.post.NullPostProcessingStrategy;
import uk.co.beerdragon.comjvm.jtool.post.PostProcessingStrategyChain;

/**
 * Utility for generating IDL, H, and CPP files defining COM wrappers to work with Java classes and
 * interfaces.
 */
public class GenerateCOM implements Runnable {

  private static final String UUID_FILE = "interfaces.lst";

  private Logger _logger = Logger.getLogger (getClass ().getName ());

  private final Set<String> _classes = new HashSet<String> ();

  private String _outputPath = ".";

  private ClassProcessor _classProcessor = new ClassProcessor ();

  /**
   * Sets the override logger to use for diagnostic output.
   * 
   * @param logger
   *          logger instance, not {@code null}
   */
  public void setLogger (final Logger logger) {
    _logger = Objects.requireNonNull (logger);
  }

  /**
   * Returns the logger used for diagnostic output.
   * 
   * @return the logger instance, never {@code null}
   */
  public Logger getLogger () {
    return _logger;
  }

  /**
   * Sets the root class names to be processed.
   * <p>
   * Additional classes will be processed if any of the classes referenced here have a dependency on
   * them.
   * 
   * @param classes
   *          class names, not {@code null} and not containing {@code null}
   */
  public void setClasses (final Collection<String> classes) {
    Validate.noNullElements (classes);
    _classes.clear ();
    _classes.addAll (classes);
  }

  /**
   * Returns the root class names to be processed.
   * 
   * @return class names, never {@code null} or containing {@code null}
   */
  public Collection<String> getClasses () {
    return new ArrayList<String> (_classes);
  }

  /**
   * Adds a class to the root set to be processed.
   * 
   * @param className
   *          class to add, not {@code null}
   */
  public void addClass (final String className) {
    _classes.add (Objects.requireNonNull (className));
  }

  /**
   * Removes a class from the root set to be processed.
   * 
   * @param className
   *          class to remove
   */
  public void removeClass (final String className) {
    _classes.remove (className);
  }

  /**
   * Sets the output directory where all files will be written.
   * <p>
   * If not set, the default will be the current directory. If the folder does not exist then it
   * will be created.
   * 
   * @param outputPath
   *          output directory, not {@code null}
   */
  public void setOutputPath (final String outputPath) {
    _outputPath = Objects.requireNonNull (outputPath);
  }

  /**
   * Returns the output directory where all files will be written.
   * 
   * @return output directory, not {@code null}
   */
  public String getOutputPath () {
    return _outputPath;
  }

  /**
   * Sets a replacement {@link ClassProcessor} instance that will handle each of the classes.
   * 
   * @param classProcessor
   *          replacement instance, not {@code null}
   */
  protected void setClassProcessor (final ClassProcessor classProcessor) {
    _classProcessor = Objects.requireNonNull (classProcessor);
  }

  /**
   * Returns the {@link ClassProcessor} instance that will handle each of the classes.
   * 
   * @return class processor instance, never {@code null}
   */
  protected ClassProcessor getClassProcessor () {
    return _classProcessor;
  }

  private UUIDManager createUUIDManager () {
    try {
      return new UUIDManager (new FileReader (new File (new File (getOutputPath ()), UUID_FILE)));
    } catch (final FileNotFoundException e) {
      // Ignore
    } catch (final IOException e) {
      getLogger ().severe ("Invalid UUID file - " + e.toString ());
    }
    return new UUIDManager ();
  }

  private void saveUUIDManagerState () {
    try {
      getClassProcessor ().getUUIDManager ().save (
          new FileWriter (new File (new File (getOutputPath ()), UUID_FILE)));
    } catch (final IOException e) {
      getLogger ().severe ("Couldn't write UUID file - " + e.toString ());
    }
  }

  private void configureCP (final Set<String> queued, final List<String> queue) {
    getClassProcessor ().setLogger (getLogger ());
    getClassProcessor ().setAdditionalClasses (new AdditionalClasses () {

      @Override
      public void add (final String className) {
        if (queued.add (className)) {
          getLogger ().info/* finest */("Deferring " + className);
          queue.add (className);
        }
      }

    });
    final File outputDir = new File (getOutputPath ());
    outputDir.mkdirs ();
    getClassProcessor ().setCppGenerator (
        new FileGenerator (outputDir, new FileExtensionStrategy (new FilePerClassStrategy (),
            CppFileContent.EXT), new NullPostProcessingStrategy ()));
    getClassProcessor ().setHppGenerator (
        new FileGenerator (outputDir, new FileExtensionStrategy (new FilePerPackageStrategy (),
            HppFileContent.EXT), new NullPostProcessingStrategy ()));
    getClassProcessor ().setIdlGenerator (
        new FileGenerator (outputDir, new FileExtensionStrategy (new FilePerPackageStrategy (),
            IdlFileContent.EXT), new PostProcessingStrategyChain (new IdlPostProcessor (outputDir),
            IdlPostProcessor.HEADER)));
    getClassProcessor ().setUUIDManager (createUUIDManager ());
  }

  private void postProcess () {
    try {
      getClassProcessor ().getCppGenerator ().postProcess ();
      getClassProcessor ().getHppGenerator ().postProcess ();
      getClassProcessor ().getIdlGenerator ().postProcess ();
    } catch (final IOException e) {
      getLogger ().severe ("Couldn't post process intermediate output - " + e.toString ());
    }
  }

  // Runnable

  @Override
  public void run () {
    getLogger ().info/* config */("Writing COM interfaces to " + getOutputPath ());
    final Set<String> queued = new HashSet<String> (_classes);
    final ArrayList<String> queue = new ArrayList<String> (_classes);
    configureCP (queued, queue);
    while (!queue.isEmpty ()) {
      final String className = queue.remove (queue.size () - 1);
      getLogger ().info/* fine */("Reading " + className);
      getClassProcessor ().process (className);
    }
    postProcess ();
    saveUUIDManagerState ();
    getLogger ().info (
        "Wrote COM interface for " + queued.size () + " class(es) in " + 0 + " package(s)");
  }

  // Program entry point

  /**
   * Runs the generator using command line parameters. For example:
   * <p>
   * java uk.co.beerdragon.comjvm.jtool.GenerateCOM -d/path/to/output/folder java.lang.Iterable
   * 
   * @param args
   *          command line arguments, not {@code null} and not containing {@code null}
   */
  public static void main (final String[] args) {
    final GenerateCOM instance = new GenerateCOM ();
    for (final String arg : args) {
      if (arg.startsWith ("-d")) {
        instance.setOutputPath (arg.substring (2));
      } else {
        instance.addClass (arg);
      }
    }
    instance.run ();
  }

}