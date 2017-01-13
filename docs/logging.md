Logging
=======

# Background
A key part of troubleshooting software development is logging.  XL4J has two independent logging systems:
 1. A bespoke C++ logging system for the native portion of the Excel Add-in.
 2. A standard SLF4J/Logback logging system for the Java-based portion of the Excel Add-in.

The reason for having two separate systems is that
 1. Java hasn't necessarily been started yet by the native parts of the Add-in when logging is required.
 2. Calling from C++ into Java for every logging statement would result is very poor performance.
 3. The alternative of having Java use the C++ logging framework might be possible, but would potentially require constant calls across
    the JNI/JVM boundary, which again, can lead to poor performance.
    
In practice, C++ logging is primarily for the development of new native (C++) features so most Add-in developers are likely to find Java 
logs more useful.

# Configuration
Configuration of the logging system is covered in the [Settings](https://github.com/McLeodMoores/xl4j/blob/master/docs/settings.md) guide
but as an overview, you can configure each logging system to produce output from log statements at a number of different logging levels.
These are summarised below:

|-----------|--|
| Log Level | Purpose |
|-----------|---------|
| TRACE     | Notifying the developer of very fine-grained flow of execution through the code, this provides typically almost excessive detail |
| DEBUG     | Information that's likely to be useful only when examining the code, but at a slightly more abstract level |
| INFO      | General informational messages about the state of execution |
| WARN      | Messages |
