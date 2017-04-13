rem Set JAVA_HOME="C:\Program Files (x86)\Java\jre7"
rem Set JAVA_HOME="C:\IBM\ibm-jdk\"
%JAVA_HOME%/jre/bin/java -Djava.ext.dirs=%JAVA_HOME%/jre/lib/ext;C:/RTC403Dev/installs/PlainJavaAPI -cp ./bin/ com.ibm.js.team.workitem.automation.examples.ModifyWorkItemAddCommentOperation "https://clm.example.com:9443/ccm" "ralph" "ralph" "54" "Add a comment"