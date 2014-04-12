resolvers += Resolver.url("bintray-sbt-plugin-releases-masseguillaume",
  url("http://dl.bintray.com/masseguillaume/maven")
)(Resolver.ivyStylePatterns)

addSbtPlugin("com.github.sbt" % "sbt-scalabuff" % "1.3.7")
