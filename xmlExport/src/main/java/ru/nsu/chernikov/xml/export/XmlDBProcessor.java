package ru.nsu.chernikov.xml.export;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class XmlDBProcessor implements Runnable {

  private static final int BATCH_SIZE = 5000;

  private final PreparedStatement putPerson;
  private final PreparedStatement putRel;
  private final File file;

  public XmlDBProcessor(PreparedStatement putPerson, PreparedStatement putRel, File file) {
    this.putPerson = putPerson;
    this.putRel = putRel;
    this.file = file;
  }

  private void addBatch(PreparedStatement s, String... params) throws SQLException {
    for (int i = 1; i <= params.length; i++) {
      s.setString(i, params[i - 1]);
    }
    s.addBatch();
  }

  @Override
  public void run() {
    try {
      XMLStreamReader xmlReader =
          XMLInputFactory.newInstance()
              .createXMLStreamReader(file.getName(), new BufferedReader(new FileReader(file)));
      Person currPerson = null;
      int pCnt = 0;
      int rCnt = 0;
      while (xmlReader.hasNext()) {
        xmlReader.next();
        if (xmlReader.isStartElement()) {
          switch (xmlReader.getLocalName()) {
            case "person":
              currPerson = new Person();
              currPerson.setId(xmlReader.getAttributeValue(0));
              currPerson.setFirstname(xmlReader.getAttributeValue(1));
              currPerson.setLastName(xmlReader.getAttributeValue(2));
              break;
            case "gender":
              xmlReader.next();
              assert currPerson != null;
              currPerson.setGender(xmlReader.getText().trim());
              break;
            case "mother":
              assert currPerson != null;
              currPerson.setMother(xmlReader.getAttributeValue(0));
              break;
            case "father":
              assert currPerson != null;
              currPerson.setFather(xmlReader.getAttributeValue(0));
              break;
            case "spouse":
              assert currPerson != null;
              currPerson.setSpouseId(xmlReader.getAttributeValue(0));
              break;
            case "sister":
              assert currPerson != null;
              currPerson.getSisters().add(xmlReader.getAttributeValue(0));
              break;
            case "brother":
              assert currPerson != null;
              currPerson.getBrothers().add(xmlReader.getAttributeValue(0));
              break;
            case "son":
              assert currPerson != null;
              currPerson.getSons().add(xmlReader.getAttributeValue(0));
              break;
            case "daughter":
              assert currPerson != null;
              currPerson.getDaughters().add(xmlReader.getAttributeValue(0));
              break;
          }
        } else if (xmlReader.isEndElement() && xmlReader.getLocalName().equals("person")) {
          assert currPerson != null;
          pCnt++;
          addBatch(
              putPerson,
              currPerson.getId(),
              currPerson.getGender(),
              currPerson.getFirstname(),
              currPerson.getLastName());

          if (currPerson.getMother() != null) {
            rCnt++;
            addBatch(putRel, currPerson.getMother(), "MOTHER", currPerson.getId());
          }
          if (currPerson.getFather() != null) {
            rCnt++;
            addBatch(putRel, currPerson.getFather(), "FATHER", currPerson.getId());
          }
          if (currPerson.getSpouseId() != null) {
            rCnt++;
            addBatch(putRel, currPerson.getSpouseId(), "SPOUSE", currPerson.getId());
          }
          for (String sister : currPerson.getSisters()) {
            rCnt++;
            addBatch(putRel, sister, "SISTER", currPerson.getId());
          }
          for (String brother : currPerson.getBrothers()) {
            rCnt++;
            addBatch(putRel, brother, "BROTHER", currPerson.getId());
          }
          for (String son : currPerson.getSons()) {
            rCnt++;
            addBatch(putRel, son, "SON", currPerson.getId());
          }
          for (String daughter : currPerson.getDaughters()) {
            rCnt++;
            addBatch(putRel, daughter, "DAUGHTER", currPerson.getId());
          }
          if (pCnt >= BATCH_SIZE) {
            putPerson.executeBatch();
            pCnt = 0;
          }
          if (rCnt >= BATCH_SIZE) {
            putRel.executeBatch();
            rCnt = 0;
          }
        }
      }
      putPerson.executeBatch();
      putRel.executeBatch();
    } catch (SQLException | XMLStreamException | FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}
