$env:user_path=Resolve-Path ~
$env:JAVA_HOME = $env:user_path + "\Fiji.app\java\win64\temurin-21.0.2\"
$env:PATH = $env:user_path + "\IdeaProjects\apache-maven-3.9.6\bin;" + $Env:PATH

