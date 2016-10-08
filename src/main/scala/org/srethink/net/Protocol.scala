package org.srethink.net

object Version {
  val V0_1      = 0x3f61ba36
  val V0_2      = 0x723081e1 // Authorization key during handshake
  val V0_3      = 0x5f75e83e // Authorization key and protocol during handshake
  val V0_4      = 0x400c2d20 // Queries execute in parallel
  val V1_0      = 0x34c2bdc3 // Users and permissions
}

object Protocol {
  val PROTOBUF  = 0x271ffc41;
  val JSON      = 0x7e6970c7;
}

object QueryType {
  val START        = 1; // Start a new query.
  val CONTINUE     = 2; // Continue a query that returned [SUCCESS_PARTIAL]
                        // (see [Response]).
  val STOP         = 3; // Stop a query partway through executing.
  val NOREPLY_WAIT = 4; // Wait for noreply operations to finish.
  val SERVER_INFO  = 5; // Get server information.
}

object ResponseType {
  val SUCCESS_ATOM      = 1;
  val SUCCESS_SEQUENCE  = 2;
  val SUCCESS_PARTIAL   = 3;
  val WAIT_COMPLETE     = 4;
  val SERVER_INFO       = 5;
  val CLIENT_ERROR      = 16;
  val COMPILE_ERROR     = 17;
  val RUNTIME_ERROR     = 18;
}

object DatumType {
  val R_NULL   = 1;
  val R_BOOL   = 2;
  val R_NUM    = 3;
  val R_STR    = 4;
  val R_ARRAY  = 5;
  val R_OBJECT = 6;
  // This [DatumType] will only be used if [accepts_r_json] is
  // set to [true] in [Query].  [r_str] will be filled with a
  // JSON encoding of the [Datum].
  val R_JSON   = 7; // uses r_str
}

object TermType {
  // A RQL datum, stored in `datum` below.
  val DATUM = 1;

  val MAKE_ARRAY = 2; // DATUM... -> ARRAY
                      // Evaluate the terms in [optargs] and make an object
  val MAKE_OBJ   = 3; // {...} -> OBJECT

  // * Compound types

  // Takes an integer representing a variable and returns the value stored
  // in that variable.  It's the responsibility of the client to translate
  // from their local representation of a variable to a unique _non-negative_
  // integer for that variable.  (We do it this way instead of letting
  // clients provide variable names as strings to discourage
  // variable-capturing client libraries, and because it's more efficient
  // on the wire.)
  val VAR          = 10; // !NUMBER -> DATUM
                         // Takes some javascript code and executes it.
  val JAVASCRIPT   = 11; // STRING {timeout: !NUMBER} -> DATUM |
                         // STRING {timeout: !NUMBER} -> Function(*)
  val UUID = 169; // () -> DATUM

  // Takes an HTTP URL and gets it.  If the get succeeds and
  //  returns valid JSON, it is converted into a DATUM
  val HTTP = 153; // STRING {data: OBJECT | STRING,
                  //         timeout: !NUMBER,
                  //         method: STRING,
                  //         params: OBJECT,
                  //         header: OBJECT | ARRAY,
                  //         attempts: NUMBER,
                  //         redirects: NUMBER,
                  //         verify: BOOL,
                  //         page: FUNC | STRING,
                  //         page_limit: NUMBER,
                  //         auth: OBJECT,
                  //         result_format: STRING,
                  //         } -> STRING | STREAM

  // Takes a string and throws an error with that message.
  // Inside of a `default` block, you can omit the first
  // argument to rethrow whatever error you catch (this is most
  // useful as an argument to the `default` filter optarg).
  val ERROR        = 12; // STRING -> Error | -> Error
                         // Takes nothing and returns a reference to the implicit variable.
  val IMPLICIT_VAR = 13; // -> DATUM

  // * Data Operators
  // Returns a reference to a database.
  val DB    = 14; // STRING -> Database
                  // Returns a reference to a table.
  val TABLE = 15; // Database, STRING, {read_mode:STRING, identifier_format:STRING} -> Table
                  // STRING, {read_mode:STRING, identifier_format:STRING} -> Table
                  // Gets a single element from a table by its primary or a secondary key.
  val GET   = 16; // Table, STRING -> SingleSelection | Table, NUMBER -> SingleSelection |
                  // Table, STRING -> NULL            | Table, NUMBER -> NULL |
  val GET_ALL = 78; // Table, DATUM..., {index:!STRING} => ARRAY

  // Simple DATUM Ops
  val EQ  = 17; // DATUM... -> BOOL
  val NE  = 18; // DATUM... -> BOOL
  val LT  = 19; // DATUM... -> BOOL
  val LE  = 20; // DATUM... -> BOOL
  val GT  = 21; // DATUM... -> BOOL
  val GE  = 22; // DATUM... -> BOOL
  val NOT = 23; // BOOL -> BOOL
                // ADD can either add two numbers or concatenate two arrays.
  val ADD = 24; // NUMBER... -> NUMBER | STRING... -> STRING
  val SUB = 25; // NUMBER... -> NUMBER
  val MUL = 26; // NUMBER... -> NUMBER
  val DIV = 27; // NUMBER... -> NUMBER
  val MOD = 28; // NUMBER, NUMBER -> NUMBER

  val FLOOR = 183;    // NUMBER -> NUMBER
  val CEIL = 184;     // NUMBER -> NUMBER
  val ROUND = 185;    // NUMBER -> NUMBER

  // DATUM Array Ops
  // Append a single element to the end of an array (like `snoc`).
  val APPEND = 29; // ARRAY, DATUM -> ARRAY
                   // Prepend a single element to the end of an array (like `cons`).
  val PREPEND = 80; // ARRAY, DATUM -> ARRAY
                    //Remove the elements of one array from another array.
  val DIFFERENCE = 95; // ARRAY, ARRAY -> ARRAY

  // DATUM Set Ops
  // Set ops work on arrays. They don't use actual sets and thus have
  // performance characteristics you would expect from arrays rather than
  // from sets. All set operations have the post condition that they
  // array they return contains no duplicate values.
  val SET_INSERT = 88; // ARRAY, DATUM -> ARRAY
  val SET_INTERSECTION = 89; // ARRAY, ARRAY -> ARRAY
  val SET_UNION = 90; // ARRAY, ARRAY -> ARRAY
  val SET_DIFFERENCE = 91; // ARRAY, ARRAY -> ARRAY

  val SLICE  = 30; // Sequence, NUMBER, NUMBER -> Sequence
  val SKIP  = 70; // Sequence, NUMBER -> Sequence
  val LIMIT = 71; // Sequence, NUMBER -> Sequence
  val OFFSETS_OF = 87; // Sequence, DATUM -> Sequence | Sequence, Function(1) -> Sequence
  val CONTAINS = 93; // Sequence, (DATUM | Function(1))... -> BOOL

  // Stream/Object Ops
  // Get a particular field from an object, or map that over a
  // sequence.
  val GET_FIELD  = 31; // OBJECT, STRING -> DATUM
                       // | Sequence, STRING -> Sequence
                       // Return an array containing the keys of the object.
  val KEYS = 94; // OBJECT -> ARRAY
                 // Return an array containing the values of the object.
  val VALUES = 186; // OBJECT -> ARRAY
                    // Creates an object
  val OBJECT = 143; // STRING, DATUM, ... -> OBJECT
                    // Check whether an object contains all the specified fields,
                    // or filters a sequence so that all objects inside of it
                    // contain all the specified fields.
  val HAS_FIELDS = 32; // OBJECT, Pathspec... -> BOOL
                       // x.with_fields(...) <=> x.has_fields(...).pluck(...)
  val WITH_FIELDS = 96; // Sequence, Pathspec... -> Sequence
                        // Get a subset of an object by selecting some attributes to preserve,
                        // or map that over a sequence.  (Both pick and pluck, polymorphic.)
  val PLUCK    = 33; // Sequence, Pathspec... -> Sequence | OBJECT, Pathspec... -> OBJECT
                     // Get a subset of an object by selecting some attributes to discard, or
                     // map that over a sequence.  (Both unpick and without, polymorphic.)
  val WITHOUT  = 34; // Sequence, Pathspec... -> Sequence | OBJECT, Pathspec... -> OBJECT
                     // Merge objects (right-preferential)
  val MERGE    = 35; // OBJECT... -> OBJECT | Sequence -> Sequence

  // Sequence Ops
  // Get all elements of a sequence between two values.
  // Half-open by default, but the openness of either side can be
  // changed by passing 'closed' or 'open for `right_bound` or
  // `left_bound`.
  val BETWEEN_DEPRECATED = 36; // Deprecated version of between, which allows `null` to specify unboundedness
                               // With the newer version, clients should use `r.minval` and `r.maxval` for unboundedness
  val BETWEEN   = 182; // StreamSelection, DATUM, DATUM, {index:!STRING, right_bound:STRING, left_bound:STRING} -> StreamSelection
  val REDUCE    = 37; // Sequence, Function(2) -> DATUM
  val MAP       = 38; // Sequence, Function(1) -> Sequence
                      // The arity of the function should be
                      // Sequence..., Function(sizeof...(Sequence)) -> Sequence

  val FOLD      = 187; // Sequence, Datum, Function(2), {Function(3), Function(1)

  // Filter a sequence with either a function or a shortcut
  // object (see API docs for details).  The body of FILTER is
  // wrapped in an implicit `.default(false)`, and you can
  // change the default value by specifying the `default`
  // optarg.  If you make the default `r.error`, all errors
  // caught by `default` will be rethrown as if the `default`
  // did not exist.
  val FILTER    = 39; // Sequence, Function(1), {default:DATUM} -> Sequence |
                      // Sequence, OBJECT, {default:DATUM} -> Sequence
                      // Map a function over a sequence and then concatenate the results together.
  val CONCAT_MAP = 40; // Sequence, Function(1) -> Sequence
                       // Order a sequence based on one or more attributes.
  val ORDER_BY   = 41; // Sequence, (!STRING | Ordering)..., {index: (!STRING | Ordering)} -> Sequence
                       // Get all distinct elements of a sequence (like `uniq`).
  val DISTINCT  = 42; // Sequence -> Sequence
                      // Count the number of elements in a sequence, or only the elements that match
                      // a given filter.
  val COUNT     = 43; // Sequence -> NUMBER | Sequence, DATUM -> NUMBER | Sequence, Function(1) -> NUMBER
  val IS_EMPTY = 86; // Sequence -> BOOL
                     // Take the union of multiple sequences (preserves duplicate elements! (use distinct)).
  val UNION     = 44; // Sequence... -> Sequence
                      // Get the Nth element of a sequence.
  val NTH       = 45; // Sequence, NUMBER -> DATUM
                      // do NTH or GET_FIELD depending on target object
  val BRACKET            = 170; // Sequence | OBJECT, NUMBER | STRING -> DATUM
                                // OBSOLETE_GROUPED_MAPREDUCE = 46;
                                // OBSOLETE_GROUPBY = 47;

  val INNER_JOIN         = 48; // Sequence, Sequence, Function(2) -> Sequence
  val OUTER_JOIN         = 49; // Sequence, Sequence, Function(2) -> Sequence
                               // An inner-join that does an equality comparison on two attributes.
  val EQ_JOIN            = 50; // Sequence, !STRING, Sequence, {index:!STRING} -> Sequence
  val ZIP                = 72; // Sequence -> Sequence
  val RANGE              = 173; // -> Sequence                        [0, +inf)
                                // NUMBER -> Sequence                 [0, a)
                                // NUMBER, NUMBER -> Sequence         [a, b)

  // Array Ops
  // Insert an element in to an array at a given index.
  val INSERT_AT          = 82; // ARRAY, NUMBER, DATUM -> ARRAY
                               // Remove an element at a given index from an array.
  val DELETE_AT          = 83; // ARRAY, NUMBER -> ARRAY |
                               // ARRAY, NUMBER, NUMBER -> ARRAY
                               // Change the element at a given index of an array.
  val CHANGE_AT          = 84; // ARRAY, NUMBER, DATUM -> ARRAY
                               // Splice one array in to another array.
  val SPLICE_AT          = 85; // ARRAY, NUMBER, ARRAY -> ARRAY

  // * Type Ops
  // Coerces a datum to a named type (e.g. "bool").
  // If you previously used `stream_to_array`, you should use this instead
  // with the type "array".
  val COERCE_TO = 51; // Top, STRING -> Top
                      // Returns the named type of a datum (e.g. TYPE_OF(true) = "BOOL")
  val TYPE_OF = 52; // Top -> STRING

  // * Write Ops (the OBJECTs contain data about number of errors etc.)
  // Updates all the rows in a selection.  Calls its Function with the row
  // to be updated, and then merges the result of that call.
  val UPDATE   = 53; // StreamSelection, Function(1), {non_atomic:BOOL, durability:STRING, return_changes:BOOL} -> OBJECT |
                     // SingleSelection, Function(1), {non_atomic:BOOL, durability:STRING, return_changes:BOOL} -> OBJECT |
                     // StreamSelection, OBJECT,      {non_atomic:BOOL, durability:STRING, return_changes:BOOL} -> OBJECT |
                     // SingleSelection, OBJECT,      {non_atomic:BOOL, durability:STRING, return_changes:BOOL} -> OBJECT
                     // Deletes all the rows in a selection.
  val DELETE   = 54; // StreamSelection, {durability:STRING, return_changes:BOOL} -> OBJECT | SingleSelection -> OBJECT
                     // Replaces all the rows in a selection.  Calls its Function with the row
                     // to be replaced, and then discards it and stores the result of that
                     // call.
  val REPLACE  = 55; // StreamSelection, Function(1), {non_atomic:BOOL, durability:STRING, return_changes:BOOL} -> OBJECT | SingleSelection, Function(1), {non_atomic:BOOL, durability:STRING, return_changes:BOOL} -> OBJECT
                     // Inserts into a table.  If `conflict` is replace, overwrites
                     // entries with the same primary key.  If `conflict` is
                     // update, does an update on the entry.  If `conflict` is
                     // error, or is omitted, conflicts will trigger an error.
  val INSERT   = 56; // Table, OBJECT, {conflict:STRING, durability:STRING, return_changes:BOOL} -> OBJECT | Table, Sequence, {conflict:STRING, durability:STRING, return_changes:BOOL} -> OBJECT

  // * Administrative OPs
  // Creates a database with a particular name.
  val DB_CREATE     = 57; // STRING -> OBJECT
                          // Drops a database with a particular name.
  val DB_DROP       = 58; // STRING -> OBJECT
                          // Lists all the databases by name.  (Takes no arguments)
  val DB_LIST       = 59; // -> ARRAY
                          // Creates a table with a particular name in a particular
                          // database.  (You may omit the first argument to use the
                          // default database.)
  val TABLE_CREATE  = 60; // Database, STRING, {primary_key:STRING, shards:NUMBER, replicas:NUMBER, primary_replica_tag:STRING} -> OBJECT
                          // Database, STRING, {primary_key:STRING, shards:NUMBER, replicas:OBJECT, primary_replica_tag:STRING} -> OBJECT
                          // STRING, {primary_key:STRING, shards:NUMBER, replicas:NUMBER, primary_replica_tag:STRING} -> OBJECT
                          // STRING, {primary_key:STRING, shards:NUMBER, replicas:OBJECT, primary_replica_tag:STRING} -> OBJECT
                          // Drops a table with a particular name from a particular
                          // database.  (You may omit the first argument to use the
                          // default database.)
  val TABLE_DROP    = 61; // Database, STRING -> OBJECT
                          // STRING -> OBJECT
                          // Lists all the tables in a particular database.  (You may
                          // omit the first argument to use the default database.)
  val TABLE_LIST    = 62; // Database -> ARRAY
                          //  -> ARRAY
                          // Returns the row in the `rethinkdb.table_config` or `rethinkdb.db_config` table
                          // that corresponds to the given database or table.
  val CONFIG  = 174; // Database -> SingleSelection
                     // Table -> SingleSelection
                     // Returns the row in the `rethinkdb.table_status` table that corresponds to the
                     // given table.
  val STATUS  = 175; // Table -> SingleSelection
                     // Called on a table, waits for that table to be ready for read/write operations.
                     // Called on a database, waits for all of the tables in the database to be ready.
                     // Returns the corresponding row or rows from the `rethinkdb.table_status` table.
  val WAIT    = 177; // Table -> OBJECT
                     // Database -> OBJECT
                     // Generates a new config for the given table, or all tables in the given database
                     // The `shards` and `replicas` arguments are required. If `emergency_repair` is
                     // specified, it will enter a completely different mode of repairing a table
                     // which has lost half or more of its replicas.
  val RECONFIGURE   = 176; // Database|Table, {shards:NUMBER, replicas:NUMBER [,
                           //                  dry_run:BOOLEAN]
                           //                 } -> OBJECT
                           // Database|Table, {shards:NUMBER, replicas:OBJECT [,
                           //                  primary_replica_tag:STRING,
                           //                  nonvoting_replica_tags:ARRAY,
                           //                  dry_run:BOOLEAN]
                           //                 } -> OBJECT
                           // Table, {emergency_repair:STRING, dry_run:BOOLEAN} -> OBJECT
                           // Balances the table's shards but leaves everything else the same. Can also be
                           // applied to an entire database at once.
  val REBALANCE     = 179; // Table -> OBJECT
                           // Database -> OBJECT

  // Ensures that previously issued soft-durability writes are complete and
  // written to disk.
  val SYNC          = 138; // Table -> OBJECT

  // Set global, database, or table-specific permissions
  val GRANT         = 188; //          -> OBJECT
                           // Database -> OBJECT
                           // Table    -> OBJECT

  // * Secondary indexes OPs
  // Creates a new secondary index with a particular name and definition.
  val INDEX_CREATE = 75; // Table, STRING, Function(1), {multi:BOOL} -> OBJECT
                         // Drops a secondary index with a particular name from the specified table.
  val INDEX_DROP   = 76; // Table, STRING -> OBJECT
                         // Lists all secondary indexes on a particular table.
  val INDEX_LIST   = 77; // Table -> ARRAY
                         // Gets information about whether or not a set of indexes are ready to
                         // be accessed. Returns a list of objects that look like this:
                         // {index:STRING, ready:BOOL[, progress:NUMBER]}
  val INDEX_STATUS = 139; // Table, STRING... -> ARRAY
                          // Blocks until a set of indexes are ready to be accessed. Returns the
                          // same values INDEX_STATUS.
  val INDEX_WAIT = 140; // Table, STRING... -> ARRAY
                        // Renames the given index to a new name
  val INDEX_RENAME = 156; // Table, STRING, STRING, {overwrite:BOOL} -> OBJECT

  // * Control Operators
  // Calls a function on data
  val FUNCALL  = 64; // Function(*), DATUM... -> DATUM
                     // Executes its first argument, and returns its second argument if it
                     // got [true] or its third argument if it got [false] (like an `if`
                     // statement).
  val BRANCH  = 65; // BOOL, Top, Top -> Top
                    // Returns true if any of its arguments returns true (short-circuits).
  val OR      = 66; // BOOL... -> BOOL
                    // Returns true if all of its arguments return true (short-circuits).
  val AND     = 67; // BOOL... -> BOOL
                    // Calls its Function with each entry in the sequence
                    // and executes the array of terms that Function returns.
  val FOR_EACH = 68; // Sequence, Function(1) -> OBJECT

  ////////////////////////////////////////////////////////////////////////////////
  ////////// Special Terms
  ////////////////////////////////////////////////////////////////////////////////

  // An anonymous function.  Takes an array of numbers representing
  // variables (see [VAR] above), and a [Term] to execute with those in
  // scope.  Returns a function that may be passed an array of arguments,
  // then executes the Term with those bound to the variable names.  The
  // user will never construct this directly.  We use it internally for
  // things like `map` which take a function.  The "arity" of a [Function] is
  // the number of arguments it takes.
  // For example, here's what `_X_.map{|x| x+2}` turns into:
  // Term {
  //   type = MAP;
  //   args = [_X_,
  //           Term {
  //             type = Function;
  //             args = [Term {
  //                       type = DATUM;
  //                       datum = Datum {
  //                         type = R_ARRAY;
  //                         r_array = [Datum { type = R_NUM; r_num = 1; }];
  //                       };
  //                     },
  //                     Term {
  //                       type = ADD;
  //                       args = [Term {
  //                                 type = VAR;
  //                                 args = [Term {
  //                                           type = DATUM;
  //                                           datum = Datum { type = R_NUM;
  //                                                           r_num = 1};
  //                                         }];
  //                               },
  //                               Term {
  //                                 type = DATUM;
  //                                 datum = Datum { type = R_NUM; r_num = 2; };
  //                               }];
  //                     }];
  //           }];
  val FUNC = 69; // ARRAY, Top -> ARRAY -> Top

  // Indicates to ORDER_BY that this attribute is to be sorted in ascending order.
  val ASC = 73; // !STRING -> Ordering
                // Indicates to ORDER_BY that this attribute is to be sorted in descending order.
  val DESC = 74; // !STRING -> Ordering

  // Gets info about anything.  INFO is most commonly called on tables.
  val INFO = 79; // Top -> OBJECT

  // `a.match(b)` returns a match object if the string `a`
  // matches the regular expression `b`.
  val MATCH = 97; // STRING, STRING -> DATUM

  // Change the case of a string.
  val UPCASE   = 141; // STRING -> STRING
  val DOWNCASE = 142; // STRING -> STRING

  // Select a number of elements from sequence with uniform distribution.
  val SAMPLE = 81; // Sequence, NUMBER -> Sequence

  // Evaluates its first argument.  If that argument returns
  // NULL or throws an error related to the absence of an
  // expected value (for instance, accessing a non-existent
  // field or adding NULL to an integer), DEFAULT will either
  // return its second argument or execute it if it's a
  // function.  If the second argument is a function, it will be
  // passed either the text of the error or NULL as its
  // argument.
  val DEFAULT = 92; // Top, Top -> Top

  // Parses its first argument as a json string and returns it as a
  // datum.
  val JSON = 98; // STRING -> DATUM
                 // Returns the datum as a JSON string.
                 // N.B.: we would really prefer this be named TO_JSON and that exists as
                 // an alias in Python and JavaScript drivers; however it conflicts with the
                 // standard `to_json` method defined by Ruby's standard json library.
  val TO_JSON_STRING = 172; // DATUM -> STRING

  // Parses its first arguments as an ISO 8601 time and returns it as a
  // datum.
  val ISO8601 = 99; // STRING -> PSEUDOTYPE(TIME)
                    // Prints a time as an ISO 8601 time.
  val TO_ISO8601 = 100; // PSEUDOTYPE(TIME) -> STRING

  // Returns a time given seconds since epoch in UTC.
  val EPOCH_TIME = 101; // NUMBER -> PSEUDOTYPE(TIME)
                        // Returns seconds since epoch in UTC given a time.
  val TO_EPOCH_TIME = 102; // PSEUDOTYPE(TIME) -> NUMBER

  // The time the query was received by the server.
  val NOW = 103; // -> PSEUDOTYPE(TIME)
                 // Puts a time into an ISO 8601 timezone.
  val IN_TIMEZONE = 104; // PSEUDOTYPE(TIME), STRING -> PSEUDOTYPE(TIME)
                         // a.during(b, c) returns whether a is in the range [b, c)
  val DURING = 105; // PSEUDOTYPE(TIME), PSEUDOTYPE(TIME), PSEUDOTYPE(TIME) -> BOOL
                    // Retrieves the date portion of a time.
  val DATE = 106; // PSEUDOTYPE(TIME) -> PSEUDOTYPE(TIME)
                  // x.time_of_day == x.date - x
  val TIME_OF_DAY = 126; // PSEUDOTYPE(TIME) -> NUMBER
                         // Returns the timezone of a time.
  val TIMEZONE = 127; // PSEUDOTYPE(TIME) -> STRING

  // These access the various components of a time.
  val YEAR = 128; // PSEUDOTYPE(TIME) -> NUMBER
  val MONTH = 129; // PSEUDOTYPE(TIME) -> NUMBER
  val DAY = 130; // PSEUDOTYPE(TIME) -> NUMBER
  val DAY_OF_WEEK = 131; // PSEUDOTYPE(TIME) -> NUMBER
  val DAY_OF_YEAR = 132; // PSEUDOTYPE(TIME) -> NUMBER
  val HOURS = 133; // PSEUDOTYPE(TIME) -> NUMBER
  val MINUTES = 134; // PSEUDOTYPE(TIME) -> NUMBER
  val SECONDS = 135; // PSEUDOTYPE(TIME) -> NUMBER

  // Construct a time from a date and optional timezone or a
  // date+time and optional timezone.
  val TIME = 136; // NUMBER, NUMBER, NUMBER, STRING -> PSEUDOTYPE(TIME) |
                  // NUMBER, NUMBER, NUMBER, NUMBER, NUMBER, NUMBER, STRING -> PSEUDOTYPE(TIME) |

  // Constants for ISO 8601 days of the week.
  val MONDAY = 107;    // -> 1
  val TUESDAY = 108;   // -> 2
  val WEDNESDAY = 109; // -> 3
  val THURSDAY = 110;  // -> 4
  val FRIDAY = 111;    // -> 5
  val SATURDAY = 112;  // -> 6
  val SUNDAY = 113;    // -> 7

  // Constants for ISO 8601 months.
  val JANUARY = 114;   // -> 1
  val FEBRUARY = 115;  // -> 2
  val MARCH = 116;     // -> 3
  val APRIL = 117;     // -> 4
  val MAY = 118;       // -> 5
  val JUNE = 119;      // -> 6
  val JULY = 120;      // -> 7
  val AUGUST = 121;    // -> 8
  val SEPTEMBER = 122; // -> 9
  val OCTOBER = 123;   // -> 10
  val NOVEMBER = 124;  // -> 11
  val DECEMBER = 125;  // -> 12

  // Indicates to MERGE to replace, or remove in case of an empty literal, the
  // other object rather than merge it.
  val LITERAL = 137; // -> Merging
                     // JSON -> Merging

  // SEQUENCE, STRING -> GROUPED_SEQUENCE | SEQUENCE, FUNCTION -> GROUPED_SEQUENCE
  val GROUP = 144;
  val SUM = 145;
  val AVG = 146;
  val MIN = 147;
  val MAX = 148;

  // `str.split()` splits on whitespace
  // `str.split(" ")` splits on spaces only
  // `str.split(" ", 5)` splits on spaces with at most 5 results
  // `str.split(nil, 5)` splits on whitespace with at most 5 results
  val SPLIT = 149; // STRING -> ARRAY | STRING, STRING -> ARRAY | STRING, STRING, NUMBER -> ARRAY | STRING, NULL, NUMBER -> ARRAY

  val UNGROUP = 150; // GROUPED_DATA -> ARRAY

  // Takes a range of numbers and returns a random number within the range
  val RANDOM = 151; // NUMBER, NUMBER {float:BOOL} -> DATUM

  val CHANGES = 152; // TABLE -> STREAM
  val ARGS = 154; // ARRAY -> SPECIAL (used to splice arguments)

  // BINARY is client-only at the moment, it is not supported on the server
  val BINARY = 155; // STRING -> PSEUDOTYPE(BINARY)

  val GEOJSON = 157;           // OBJECT -> PSEUDOTYPE(GEOMETRY)
  val TO_GEOJSON = 158;        // PSEUDOTYPE(GEOMETRY) -> OBJECT
  val POINT = 159;             // NUMBER, NUMBER -> PSEUDOTYPE(GEOMETRY)
  val LINE = 160;              // (ARRAY | PSEUDOTYPE(GEOMETRY))... -> PSEUDOTYPE(GEOMETRY)
  val POLYGON = 161;           // (ARRAY | PSEUDOTYPE(GEOMETRY))... -> PSEUDOTYPE(GEOMETRY)
  val DISTANCE = 162;          // PSEUDOTYPE(GEOMETRY), PSEUDOTYPE(GEOMETRY) {geo_system:STRING, unit:STRING} -> NUMBER
  val INTERSECTS = 163;        // PSEUDOTYPE(GEOMETRY), PSEUDOTYPE(GEOMETRY) -> BOOL
  val INCLUDES = 164;          // PSEUDOTYPE(GEOMETRY), PSEUDOTYPE(GEOMETRY) -> BOOL
  val CIRCLE = 165;            // PSEUDOTYPE(GEOMETRY), NUMBER {num_vertices:NUMBER, geo_system:STRING, unit:STRING, fill:BOOL} -> PSEUDOTYPE(GEOMETRY)
  val GET_INTERSECTING = 166;  // TABLE, PSEUDOTYPE(GEOMETRY) {index:!STRING} -> StreamSelection
  val FILL = 167;              // PSEUDOTYPE(GEOMETRY) -> PSEUDOTYPE(GEOMETRY)
  val GET_NEAREST = 168;       // TABLE, PSEUDOTYPE(GEOMETRY) {index:!STRING, max_results:NUM, max_dist:NUM, geo_system:STRING, unit:STRING} -> ARRAY
  val POLYGON_SUB = 171;       // PSEUDOTYPE(GEOMETRY), PSEUDOTYPE(GEOMETRY) -> PSEUDOTYPE(GEOMETRY)

  // Constants for specifying key ranges
  val MINVAL = 180;
  val MAXVAL = 181;
}
