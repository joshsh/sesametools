<!-- This README can be viewed at https://github.com/joshsh/sesametools/wiki -->

![SesameTools logo|width=322px|height=60px](https://github.com/joshsh/sesametools/wiki/graphics/SesameTools-logo.png)

[![Build Status](https://travis-ci.org/joshsh/sesametools.svg?branch=develop)](https://travis-ci.org/joshsh/sesametools) [![Coverage Status](https://coveralls.io/repos/joshsh/sesametools/badge.svg?branch=develop)](https://coveralls.io/r/joshsh/sesametools?branch=develop)

Welcome to the SesameTools wiki!
SesameTools is a collection of general-purpose components for use with the [Sesame](http://openrdf.org) RDF framework.  It includes:

* **SesameTools common utilities**: miscellaneous useful classes
* **CachingSail**: an in-memory cache for RDF data
* **ConstrainedSail**: a Sail implementation which interacts only with given named graphs.  Useful for simple access control.
* **DeduplicationSail**: a Sail implementation which avoids duplicate statements.  For use with triple stores such as AllegroGraph which otherwise allow duplicates.
* [LinkedDataServer](https://github.com/joshsh/sesametools/wiki/LinkedDataServer): a RESTful web service to publish a Sesame data store as Linked Data
* **MappingSail**: a Sail which translates between two URI spaces.  Used by LinkedDataServer.
* **RDFTransactionSail**: a write-only Sail which generates RDF transactions instead of performing per-call updates.  Useful for streaming RDF data as described [here](http://arxiv.org/abs/1011.3595)
* **ReadOnlySail**: a read-only Sail implementation
* **ReplaySail**: a pair of Sail implementations which allow Sail operations to be first recorded to a log file, then reproduced from the log file
* **RepositorySail**: a Sail implementation which wraps a Repository object.  This is essentially the inverse of Sesame's [SailRepository](http://www.openrdf.org/doc/sesame2/api/org/openrdf/repository/sail/SailRepository.html)
* **Sesamize**: command-line tools for Sesame
* **URI Translator**: a utility which runs SPARQL-1.1 Update queries against a Repository to convert URIs between different prefixes
* **WriteOnlySail**: a write-only Sail implementation

See also the [Sesametools API](http://fortytwo.net/projects/sesametools/api/latest/index.html).

For projects which use Maven, SesameTools snapshots and release packages can be imported by adding configuration like the following to the project's POM:

```xml
        <dependency>
            <groupId>net.fortytwo.sesametools</groupId>
            <artifactId>linked-data-server</artifactId>
            <version>1.9</version>
        </dependency>
```

The latest Maven packages can be browsed [here](http://search.maven.org/#search%7Cga%7C1%7Csesametools).

**Credits**: SesameTools is written and maintained by [Joshua Shinavier](https://github.com/joshsh) and [Peter Ansell](https://github.com/ansell). Patches have been contributed by [fkleedorfer](https://github.com/fkleedorfer). An RDF/JSON parser and writer present in releases 1.7 and earlier contain code by [Hannes Ebner](http://ebner.wordpress.com/).
