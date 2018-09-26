
=== pom.xml ===

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.felix</groupId>
        <artifactId>maven-bundle-plugin</artifactId>
        <configuration>
          <instructions>
            <Meta-Persistence>META-INF/persistence.xml</Meta-Persistence>
            <Import-Package>
              org.hibernate.proxy,
              javassist.util.proxy,
              *
            </Import-Package>
          </instructions>
        </configuration>
      </plugin>
    </plugins>
  </build>



=== META-INF/persistence.xml ===

<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.0"
    xmlns="http://java.sun.com/xml/ns/persistence"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd">

  <persistence-unit name="calculatordb" transaction-type="JTA">
    <shared-cache-mode>ENABLE_SELECTIVE</shared-cache-mode>
  </persistence-unit>

</persistence>
