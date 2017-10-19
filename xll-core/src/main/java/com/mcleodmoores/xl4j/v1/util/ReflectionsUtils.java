package com.mcleodmoores.xl4j.v1.util;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.MethodParameterScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 * Utility class to hide whether compile-time or run-time class scanning is used.
 */
public final class ReflectionsUtils {
  private ReflectionsUtils() {
  }
  
  private static boolean isProduction() {
    String scan = System.getProperty("xl4j.scan");
    if (scan != null) {
      if (scan.toLowerCase().contains("true")) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * @return a reflections object with scanners and urls initialised
   */
  public static Reflections getReflections() {
    final Reflections reflections = isProduction() ? Reflections.collect() : new Reflections(
        new ConfigurationBuilder()
            .addUrls(ClasspathHelper.forJavaClassPath())
            .addScanners(new MethodAnnotationsScanner(), new MethodParameterScanner(), 
                         new TypeAnnotationsScanner(), new FieldAnnotationsScanner(),
                         new SubTypesScanner(true)));
    return reflections;
  }
}
