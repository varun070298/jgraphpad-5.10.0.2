package org.microplatform.io;

import java.io.File;
import java.util.StringTokenizer;

import javax.swing.filechooser.FileFilter;

/**
   A file filter that accepts all files with a given set
   of extensions.
*/
public class ExtensionFilter 
   extends FileFilter
{
   /**
      Constructs an extension file filter.
      @param description the description (e.g. "Woozle files")
      @param extensions the accepted extensions (e.g.
      new String[] { ".woozle", ".wzl" })
   */
   public ExtensionFilter(String description, 
      String[] extensions)
   {
      this.description = description; 
      this.extensions = extensions;
   }

   /**
      Constructs an extension file filter.
      @param description the description (e.g. "Woozle files")
      @param extensions the accepted extensions, separated
      by | (e.g.".woozle|.wzl" })
   */
   public ExtensionFilter(String description, 
      String extensions)
   {
      this.description = description; 
      StringTokenizer tokenizer = new StringTokenizer(
         extensions, "|");
      this.extensions = new String[tokenizer.countTokens()];
      for (int i = 0; i < this.extensions.length; i++)
         this.extensions[i] = tokenizer.nextToken();
   }
   
   public boolean accept(File f)
   {  
      if (f.isDirectory()) return true;
      String fname = f.getName().toLowerCase();
      for (int i = 0; i < extensions.length; i++)
         if (fname.endsWith(extensions[i].toLowerCase())) 
            return true;
      return false;
   }
   
   public String getDescription()
   { 
      return description; 
   }
   
   public String[] getExtensions()
   {
      return extensions;
   }

   private String description;
   private String[] extensions;
}

