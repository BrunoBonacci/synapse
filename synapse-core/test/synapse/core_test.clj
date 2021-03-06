(ns synapse.core-test
  (:refer-clojure :exclude [resolve])
  (:require [synapse.core :refer :all]
            [midje.sweet :refer :all]))


(facts "resolving environment variables"

       ;; direct match
       (resolve
        {"SIMPLE" "yes"}
        {:resolver :env :target "SIMPLE"})
       => "yes"


       ;; lower case
       (resolve
        {"SIMPLE" "yes"}
        {:resolver :env :target "simple"})
       => "yes"


       ;; longer name
       (resolve
        {"SOME_OTHER_NAME123" "ok"}
        {:resolver :env :target "SOME_OTHER_NAME123"})
       => "ok"


       ;; not found
       (resolve
        {"SOME_OTHER_NAME123" "ok"}
        {:resolver :env :target "HOME"})
       => nil


       ;; multiple values
       (resolve
        {"VALUE1" "1"
         "VALUE2" "2"
         "VALUE3" "3"}
        {:resolver :env :link-type :multiple :target "VALUE.*"})
       => "1,2,3"


       ;; multiple values single match
       (resolve
        {"VALUE1" "1"}
        {:resolver :env :link-type :multiple :target "VALUE.*"})
       => "1"


       ;; multiple match but single var
       (resolve
        {"VALUE1" "1"
         "VALUE2" "2"
         "VALUE3" "3"}
        {:resolver :env :link-type :single :target "VALUE.*"})
       => "1"


       ;; multiple values but NO match
       (resolve
        {"VALUE1" "1"
         "VALUE2" "2"
         "VALUE3" "3"}
        {:resolver :env :link-type :multiple :target "SOMETHING_ELSE.*"})
       => nil

       )



(facts "resolving environment variables using prefix resolution"


       (resolve
        {"PREFIX_SIMPLE" "yes"}
        {:resolver :prefix :target "PREFIX_"})
       => "simple=yes"


       ;; lower case
       (resolve
        {"PREFIX_SIMPLE" "yes"}
        {:resolver :prefix :target "prefix_"})
       => "simple=yes"


       ;; longer name
       (resolve
        {"PREFIX_SOME_OTHER_NAME123" "ok"}
        {:resolver :prefix :target "PREFIX_"})
       => "some.other.name123=ok"


       ;; not found
       (resolve
        {"DIFF_SOME_OTHER_NAME123" "ok"}
        {:resolver :prefix :target "PREFIX_"})
       => ""


       ;; not found 2
       (resolve
        {"DIFF_SOME_OTHER_NAME123" "ok"}
        {:resolver :prefix :target "FF_"})
       => ""


       ;; multiple values
       (resolve
        {"PREFIX_VALUE1" "1"
         "PREFIX_VALUE2" "2"
         "PREFIX_VALUE3" "3"}
        {:resolver :prefix :target "PREFIX_"})
       => "value1=1\nvalue2=2\nvalue3=3"


       ;; longer name
       (resolve
        {"PREFIX_SOME_OTHER_NAME123" "ok"}
        {:resolver :prefix :target "PREFIX_" :options {:separator "" :case :camel}})
       => "someOtherName123=ok"


       )



(facts "resolving docker links: from <cnt>_PORT_<port>_TCP_ADDR and <cnt>_PORT_<port>_TCP_PORT environment variable style"


       ;; simple match
       (resolve
        {"DB_PORT_12345_TCP_ADDR" "10.10.10.10"
         "DB_PORT_12345_TCP_PORT" "54321"}
        {:resolver :docker :link-type :single :target "DB" :port "12345"})
       => "10.10.10.10:54321"


       ;; NOT matching
       (resolve
        {"DB_PORT_12345_TCP_ADDR" "10.10.10.10"
         "DB_PORT_12345_TCP_PORT" "54321"}
        {:resolver :docker :link-type :single :target "WEB" :port "12345"})
       => nil


       ;; what happen if there is a ADDR but not a port?
       ;; the port name it is used instead
       (resolve
        {"DB_PORT_12345_TCP_ADDR" "10.10.10.10"}
        {:resolver :docker :link-type :single :target "DB" :port "12345"})
       => "10.10.10.10:12345"


       ;; simple match with pattern
       (resolve
        {"DB1_PORT_12345_TCP_ADDR" "10.10.10.10"
         "DB1_PORT_12345_TCP_PORT" "54321"}
        {:resolver :docker :link-type :single :target "DB.*" :port "12345"})
       => "10.10.10.10:54321"


       ;; multiple match with pattern but single link
       (resolve
        {"DB2_PORT_12345_TCP_ADDR" "20.20.20.20"
         "DB2_PORT_12345_TCP_PORT" "54321"
         "DB1_PORT_12345_TCP_ADDR" "10.10.10.10"
         "DB1_PORT_12345_TCP_PORT" "54321"
         "DB3_PORT_12345_TCP_ADDR" "30.30.30.30"
         "DB3_PORT_12345_TCP_PORT" "54321"}
        {:resolver :docker :link-type :single :target "DB.*" :port "12345"})
       => "10.10.10.10:54321"


       ;; multiple match with pattern
       (resolve
        {"DB2_PORT_12345_TCP_ADDR" "20.20.20.20"
         "DB2_PORT_12345_TCP_PORT" "2000"
         "DB1_PORT_12345_TCP_ADDR" "10.10.10.10"
         "DB1_PORT_12345_TCP_PORT" "1000"
         "DB3_PORT_12345_TCP_ADDR" "30.30.30.30"
         "DB3_PORT_12345_TCP_PORT" "3000"}
        {:resolver :docker :link-type :multiple :target "DB.*" :port "12345"})
       => "20.20.20.20:2000,10.10.10.10:1000,30.30.30.30:3000"


       ;; multiple link with NO match
       (resolve
        {"DB2_PORT_12345_TCP_ADDR" "20.20.20.20"
         "DB2_PORT_12345_TCP_PORT" "2000"
         "DB1_PORT_12345_TCP_ADDR" "10.10.10.10"
         "DB1_PORT_12345_TCP_PORT" "1000"
         "DB3_PORT_12345_TCP_ADDR" "30.30.30.30"
         "DB3_PORT_12345_TCP_PORT" "3000"}
        {:resolver :docker :link-type :multiple :target "WEB.*" :port "12345"})
       => nil


       ;; single link with multiple ports and a specific port
       (resolve
        {"DB_PORT_22345_TCP_ADDR" "20.20.20.20"
         "DB_PORT_22345_TCP_PORT" "3333"
         "DB_PORT_12345_TCP_ADDR" "10.10.10.10"
         "DB_PORT_12345_TCP_PORT" "54321"
         "DB_PORT_32345_TCP_ADDR" "30.30.30.30"
         "DB_PORT_32345_TCP_PORT" "44444"}
        {:resolver :docker :link-type :single :target "DB" :port "12345"})
       => "10.10.10.10:54321"


       ;; single link with multiple ports and NO specific port
       ;; should take the lowest port "name"
       (resolve
        {"DB_PORT_22345_TCP_ADDR" "20.20.20.20"
         "DB_PORT_22345_TCP_PORT" "44321"
         "DB_PORT_12345_TCP_ADDR" "10.10.10.10"
         "DB_PORT_12345_TCP_PORT" "54321"
         "DB_PORT_32345_TCP_ADDR" "30.30.30.30"
         "DB_PORT_32345_TCP_PORT" "34321"}
        {:resolver :docker :link-type :single :target "DB"})
       => "10.10.10.10:54321"



       )




(facts "resolving docker links: from <cnt>_PORT_<port>_TCP environment variable style"


       ;; simple match
       (resolve
        {"DB_PORT_12345_TCP" "tcp://10.10.10.10:54321"}
        {:resolver :docker :link-type :single :target "DB" :port "12345"})
       => "10.10.10.10:54321"


       ;; NOT matching
       (resolve
        {"DB_PORT_12345_TCP" "tcp://10.10.10.10:54321"}
        {:resolver :docker :link-type :single :target "WEB" :port "12345"})
       => nil

       ;; simple match with pattern
       (resolve
        {"DB1_PORT_12345_TCP" "tcp://10.10.10.10:54321"}
        {:resolver :docker :link-type :single :target "DB.*" :port "12345"})
       => "10.10.10.10:54321"



       ;; multiple match with pattern but single link
       (resolve
        {"DB2_PORT_12345_TCP" "tcp://20.20.20.20:54321"
         "DB1_PORT_12345_TCP" "tcp://10.10.10.10:54321"
         "DB3_PORT_12345_TCP" "tcp://30.30.30.30:54321"}
        {:resolver :docker :link-type :single :target "DB.*" :port "12345"})
       => "10.10.10.10:54321"


       ;; multiple match with pattern
       (resolve
        {"DB2_PORT_12345_TCP" "tcp://20.20.20.20:2000"
         "DB1_PORT_12345_TCP" "tcp://10.10.10.10:1000"
         "DB3_PORT_12345_TCP" "tcp://30.30.30.30:3000"}
        {:resolver :docker :link-type :multiple :target "DB.*" :port "12345"})
       => "20.20.20.20:2000,10.10.10.10:1000,30.30.30.30:3000"


       ;; single link with multiple ports and a specific port
       (resolve
        {"DB_PORT_22345_TCP" "tcp://20.20.20.20:2000"
         "DB_PORT_12345_TCP" "tcp://10.10.10.10:1000"
         "DB_PORT_32345_TCP" "tcp://30.30.30.30:3000"}

        {:resolver :docker :link-type :single :target "DB" :port "12345"})
       => "10.10.10.10:1000"


       ;; single link with multiple ports and NO specific port
       ;; should take the lowest port "name"
       (resolve
        {"DB_PORT_22345_TCP" "tcp://20.20.20.20:2000"
         "DB_PORT_12345_TCP" "tcp://10.10.10.10:3000"
         "DB_PORT_32345_TCP" "tcp://30.30.30.30:1000"}
        {:resolver :docker :link-type :single :target "DB"})
       => "10.10.10.10:3000"


       ;; multiple match with pattern and mix style vars
       (resolve
        {"DB2_PORT_12345_TCP_ADDR" "20.20.20.20"
         "DB2_PORT_12345_TCP_PORT" "2000"
         "DB1_PORT_12345_TCP" "tcp://10.10.10.10:1000"
         "DB1_PORT_12345_TCP_ADDR" "10.10.10.10"
         "DB1_PORT_12345_TCP_PORT" "1000"
         "DB3_PORT_12345_TCP" "tcp://30.30.30.30:3000"}
        {:resolver :docker :link-type :multiple :target "DB.*" :port "12345"})
       => "20.20.20.20:2000,10.10.10.10:1000,30.30.30.30:3000"


       )


(facts "resolve with default value"

       (resolve
        {}
        {:resolver :env :target "DATA_DIR" :default "/data"})
       => "/data"

       (resolve-with-meta
        {}
        {:resolver :env :target "DATA_DIR" :default "/data"})
       => {:resolved "/data", :resolution :default, :sources ()}

       (resolve
        {"DATA_DIR" "/mnt/data"}
        {:resolver :env :target "DATA_DIR" :default "/data"})
       => "/mnt/data"

       (resolve-with-meta
        {"DATA_DIR" "/mnt/data"}
        {:resolver :env :target "DATA_DIR" :default "/data"})
       => {:resolved "/mnt/data", :resolution :ok, :sources [["DATA_DIR" "/mnt/data"]]}


       (resolve
        {}
        {:resolver :docker :target "database" :port 8998 :default "10.10.10.10:8765"})
       => "10.10.10.10:8765"

       (resolve-with-meta
        {}
        {:resolver :docker :target "database" :port 8998 :default "10.10.10.10:8765"})
       => {:resolved "10.10.10.10:8765", :resolution :default, :sources ()}


       (resolve
        {"DATABASE_PORT_8998_TCP" "tcp://20.20.20.20:1234"}
        {:resolver :docker :target "database" :port 8998 :default "10.10.10.10:8765"})
       => "20.20.20.20:1234"


       (resolve-with-meta
        {"DATABASE_PORT_8998_TCP" "tcp://20.20.20.20:1234"}
        {:resolver :docker :target "database" :port 8998 :default "10.10.10.10:8765"})
       => {:resolved "20.20.20.20:1234",
           :resolution :ok,
           :sources
           [{:link-name "database",
             :link-port 8998,
             :address "20.20.20.20",
             :port 1234,
             :source {"DATABASE_PORT_8998_TCP" "tcp://20.20.20.20:1234"}}]}
       )



(facts "testing docker addr and port options"


       (resolve
        {"DATABASE_PORT_8998_TCP" "tcp://20.20.20.20:1234"}
        {:resolver :docker :target "database" :port 8998 :options {:addr true}})
        => "20.20.20.20"


        (resolve
         {"DATABASE_PORT_8998_TCP" "tcp://20.20.20.20:1234"}
         {:resolver :docker :target "database" :port 8998 :options {:port true}})
        => "1234"


        (resolve
         {"DATABASE_PORT_8998_TCP" "tcp://20.20.20.20:1234"}
         {:resolver :docker :target "database" :port 8998
          :options {:addr true :port true}})
        => "20.20.20.20:1234"


        (resolve
         {"DATABASE_PORT_8998_TCP" "tcp://20.20.20.20:1234"}
         {:resolver :docker :target "database" :port 8998
          :options {}})
        => "20.20.20.20:1234"



        (resolve
         {"DATABASE2_PORT_8998_TCP" "tcp://20.20.20.20:1234"
          "DATABASE1_PORT_8998_TCP" "tcp://10.10.10.10:1234"
          "DATABASE3_PORT_8998_TCP" "tcp://30.30.30.30:1234"}
         {:resolver :docker :target "database.*" :port 8998
          :link-type :multiple :options {:addr true}})
        => "20.20.20.20,10.10.10.10,30.30.30.30"



        (resolve
         {"DATABASE2_PORT_8998_TCP" "tcp://20.20.20.20:1234"
          "DATABASE1_PORT_8998_TCP" "tcp://10.10.10.10:1234"
          "DATABASE3_PORT_8998_TCP" "tcp://30.30.30.30:1234"}
         {:resolver :docker :target "database.*" :port 8998
          :link-type :multiple :options {:port true}})
        => "1234,1234,1234"


       )






(facts "testing separator option"


        (resolve
         {"DATABASE2_PORT_8998_TCP" "tcp://20.20.20.20:1234"
          "DATABASE1_PORT_8998_TCP" "tcp://10.10.10.10:1234"
          "DATABASE3_PORT_8998_TCP" "tcp://30.30.30.30:1234"}
         {:resolver :docker :target "database.*" :port 8998
          :link-type :multiple :options {:addr true :separator ";"}})
        => "20.20.20.20;10.10.10.10;30.30.30.30"



        (resolve
         {"DATABASE2_PORT_8998_TCP" "tcp://20.20.20.20:1234"
          "DATABASE1_PORT_8998_TCP" "tcp://10.10.10.10:1234"
          "DATABASE3_PORT_8998_TCP" "tcp://30.30.30.30:1234"}
         {:resolver :env :target "database.*"
          :link-type :multiple :options {:separator "\n"}})
        => "tcp://20.20.20.20:1234\ntcp://10.10.10.10:1234\ntcp://30.30.30.30:1234"


       )
