package org.toradocu.conf;

import com.beust.jcommander.IStringConverter;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;

public class ClassDirsConverter implements IStringConverter<List<URL>> {

  private static org.slf4j.Logger log = LoggerFactory.getLogger(ClassDirsConverter.class);

  @Override
  public List<URL> convert(String pathsString) {
    final String[] paths = pathsString.split(File.pathSeparator);

    List<URL> urls = new ArrayList<>(paths.length);
    for (String path : paths) {
      try {
        urls.add(new File(path).toURI().toURL());
      } catch (MalformedURLException e) {
        log.error(
            "Provided path"
                + path
                + ". is wrong. Check the correctness of the path provided with option --class-dir.",
            e);
      }
    }
    return urls;
  }
}
