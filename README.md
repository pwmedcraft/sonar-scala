Sonar Scala Plugin
===========
Supports Sonar 4.5+ 

To include test and coverage reports:

Install these plugins in your scala project:

https://github.com/mmarich/sbt-simple-junit-xml-reporter-plugin
- Creates junit xml reports for output from scalatest.

https://github.com/sqality/scct
- Creates a Scala-friendly code-coverage report, and includes a coberura xml report.


Add the following properties to your project's sonar-project.properties file:

    sonar.junit.reportsPath=test-reports
    sonar.cobertura.reportPath=target/scala-[scala-version]/coverage-report/cobertura.xml
