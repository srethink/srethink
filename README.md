[![Stories in Ready](https://badge.waffle.io/srethink/srethink.png?label=ready&title=Ready)](https://waffle.io/srethink/srethink)
[![Build Status](https://travis-ci.org/srethink/srethink.svg?branch=master)](https://travis-ci.org/srethink/srethink)
[![Coverage Status](https://img.shields.io/coveralls/srethink/srethink.svg)](https://coveralls.io/r/srethink/srethink)

Srethink -- A rethinkdb driver for scala
*****************************************

Features
--------

+  Netty based network IO
+  Asynchronously, future based API
+  Protobuf based serialization
+  Macro generated encoder/decoder for case/normal class


Usage
-----

#### Requirements ####
Scala 2.10/2.11

Rethinkdb protocol V2/V3

#### Builds From Source ####
> Current no binary release avaliable, you had to build it by yourself

 + install `sbt`
 + clone the repo `git clone https://github.com/srethink/srethink.git`
 + run `sbt +publishLocal`

#### Add dependency ####
> build.sbt

```scala
"org.srethink" %% "srethink" % "0.0.1-SNAPSHOT",
```

#### Perform queries ####

Basic pattern:

`DSL => ASD =`encoder`=> Query => Query Executor => Response =`decoder`=> Scala object`

> Set up query executor

You can write a trait like this

```scala
trait WithRethinkdb {
  lazy val rethinkConfig = {
    RethinkConfig.nettyConfig()
  }

  implicit lazy val rethinkQueryExecutor = {
    new ManagedQueryExecutor(new NettyConnectionManager(rethinkConfig))
  }
}
```

> Define data and encoder/decoder

```scala
case class Person(
  id: Option[Long],
  name: String,
  gender: String)

object Person {
  import srethink.api._
  implicit val encoder = CodecMacros.encoder[Person]
  implicit val decoder = CodecMacros.decoder[Person]
}
```

> Inserting data then retrive

```scala
class PersonRepository extends WithRethinkdb {
  private val persons = r.table("person", "test") //test is database name

  def insert(person: Person) = persons.insert(person).run

  def getById(id: Long) = persons.get(id).first[Person]
}
```
> Check more example usage in src/test/scala
