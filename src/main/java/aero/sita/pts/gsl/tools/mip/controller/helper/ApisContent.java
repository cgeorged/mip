package aero.sita.pts.gsl.tools.mip.controller.helper;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class ApisContent {
    
    private static final Pattern SPLIT_PATTERN = Pattern.compile("<SPLIT>");    
    private static final Pattern CRLF_PATTERN = Pattern.compile("<CRLF>");
    
    
    
    public static String[] getApiPartsFromString(String originalMessage) {
        CharBuffer cb = CharBuffer.wrap(originalMessage.toCharArray());
        String[] apiParts = new String[0];
        if (cb.length() > 0)
          apiParts = getParts(cb); 
        return apiParts;
    }
    
    private static String[] getParts(CharBuffer cb) {
        String start = cb.subSequence(0, 6).toString();
        if ("&date=".equals(start))
          return getDataBlocks(cb); 
        String[] splits = getSplits(cb);
        Collection<String> parts = new ArrayList<>();
        for (String split : splits) {
          if (split.length() > 0) {
            String trimmed = removeCRLFs(split).trim();
            String part = insertControlChars(trimmed);
            parts.add(part);
          } 
        } 
        List<String> prlParts = new ArrayList<>();
        Iterator<String> pi = parts.iterator();
        while (pi.hasNext()) {
          String part = pi.next();
          int prlIndex = part.indexOf("\002PRL");
          if (prlIndex > -1) {
            prlParts.add(part);
            pi.remove();
          } 
        } 
        if (!prlParts.isEmpty()) {
          String prl = processPrl(prlParts);
          if (prl != null && prl.length() > 0)
            parts.add(prl); 
        } 
        return parts.<String>toArray(new String[0]);
      }
    
    private static String[] getDataBlocks(CharBuffer cb) {
        Pattern DATA_PATTERN = Pattern.compile("&data=");
        String[] dataBlocks = DATA_PATTERN.split(cb);
        Collection<String> parts = new ArrayList<>();
        for (String block : dataBlocks) {
          block = block.trim();
          if (block.length() > 0 && 
            !block.startsWith("&date=")) {
            block = block.replaceAll("\\r", "");
            block = block.replaceAll("\\n", "\r\n");
            int ctrlAPos = block.indexOf("\r\n\001");
            if (ctrlAPos < 0)
              block = "\r\n\001" + block; 
            int ctrlCPos = block.indexOf('\003');
            if (ctrlCPos < 0)
              block = block + "\r\n\003"; 
            parts.add(block);
          } 
        } 
        return parts.<String>toArray(new String[0]);
      }
    
    
    private static String[] getSplits(CharBuffer cb) {
        String[] splits = SPLIT_PATTERN.split(cb);
        return splits;
      }
      
    private static String removeCRLFs(String part) {
        String result = part.replaceAll("\\r\\n", "");
        result = result.replaceAll("\\n", "");
        result = CRLF_PATTERN.matcher(result).replaceAll("\r\n");
        return result;
    }
      
    private static String insertControlChars(String part) {
          StringBuilder buffer = null;
          int ctrlAPos = part.indexOf("\r\n\001");
          if (ctrlAPos < 0) {
            buffer = new StringBuilder(part.length());
            buffer.append("\r\n\001");
            buffer.append(part);
          } 
          int ctrlBPos = part.indexOf('\002');
          if (ctrlBPos < 0) {
            if (buffer == null)
              buffer = new StringBuilder(part); 
            int unaPos = buffer.indexOf("UNA");
            if (unaPos > 0) {
              buffer.insert(unaPos, '\002');
            } else {
              int unbPos = buffer.indexOf("UNB");
              if (unbPos > 0) {
                buffer.insert(unbPos, '\002');
              } else {
                int prlPos = buffer.indexOf("PRL");
                if (prlPos > 0) {
                  buffer.insert(prlPos, '\002');
                } else {
                  int corPos = buffer.indexOf("COR");
                  if (corPos > 0) {
                    buffer.insert(corPos, '\002');
                  } else {
                    int pdmPos = buffer.indexOf("PDM");
                    if (pdmPos > 0) {
                      buffer.insert(pdmPos, '\002');
                    } else {
                      int mbhPos = buffer.indexOf("MBH");
                      if (mbhPos > 0)
                        buffer.insert(mbhPos, '\002'); 
                    } 
                  } 
                } 
              } 
            } 
          } 
          int ctrlCPos = part.indexOf('\003');
          if (ctrlCPos < 0) {
            if (buffer == null)
              buffer = new StringBuilder(part); 
            buffer.append("\r\n");
            buffer.append('\003');
          } 
          if (buffer == null)
            return part; 
          return buffer.toString();
    }
    

    private static String processPrl(List<String> prlParts) {
      List<Entry> entries = new ArrayList<>();
      for (String prlPart : prlParts) {
        String[] lines = prlPart.split("\r\n");
        if (lines.length > 3) {
          int i = 0;
          for (i = 0; i < lines.length && 
            lines[i].indexOf("PART") == -1; i++);
          String[] splits = lines[i].split(" ");
          if (splits.length > 2) {
            String partNoStr = splits[2];
            if (partNoStr.startsWith("PART")) {
              String number = partNoStr.substring(4);
              entries.add(new Entry(number, lines));
            } 
          } 
        } 
      } 
      Collections.sort(entries);
      boolean firstPart = true;
      StringBuilder allParts = new StringBuilder();
      for (Entry entry : entries) {
        int partStart = 5;
        if (entry.lines.length > 6 && 
          entry.lines[5].startsWith("1"))
          partStart = 4; 
        if (firstPart) {
          partStart = 0;
          firstPart = false;
        } 
        for (int l = partStart; l < entry.lines.length; l++) {
          if (!entry.lines[l].startsWith("ENDPART") && !entry.lines[l].startsWith("ENDPRL")) {
            allParts.append(entry.lines[l]);
            allParts.append("\r\n");
          } 
        } 
      } 
      return allParts.toString();
    }
    
    private static class Entry implements Comparable<Entry> {
        String partNoStr;
        
        String[] lines;
        
        public Entry(String partNoStr, String[] lines) {
          this.partNoStr = partNoStr;
          this.lines = lines;
        }
        
        public int hashCode() {
          int PRIME = 31;
          int result = 1;
          result = 31 * result + Arrays.hashCode((Object[])this.lines);
          result = 31 * result + ((this.partNoStr == null) ? 0 : this.partNoStr.hashCode());
          return result;
        }
        
        public boolean equals(Object obj) {
          if (this == obj)
            return true; 
          if (obj == null)
            return false; 
          if (getClass() != obj.getClass())
            return false; 
          Entry other = (Entry)obj;
          if (!Arrays.equals((Object[])this.lines, (Object[])other.lines))
            return false; 
          if (this.partNoStr == null) {
            if (other.partNoStr != null)
              return false; 
          } else if (!this.partNoStr.equals(other.partNoStr)) {
            return false;
          } 
          return true;
        }
        
        public int compareTo(Entry o) {
          int thizNo = Integer.parseInt(this.partNoStr);
          int thatNo = Integer.parseInt(o.partNoStr);
          return thizNo - thatNo;
        }
      }
    

}
