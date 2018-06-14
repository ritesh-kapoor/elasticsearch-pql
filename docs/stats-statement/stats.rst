Stats Statement
===============

.. toctree::
   :maxdepth: 2
   :numbered:

.. contents:: Table of Contents
   :depth: 2
   :local:

Metrics Aggregations
--------------------
The aggregations in this family compute metrics based on values extracted in one way or another from the documents that are being aggregated.

.. code-block:: pql

    source 'employee'
     | stats minAge=min(Age),maxAge=max(Age),countAge=count(Age),
        percentilesAge=percentiles(Age),sumAge=sum(Age),cardinalityAge=cardinality(Age)


Metrics Aggregations Over Fields
--------------------------------
The clause (query) must appear in matching documents

.. code-block:: pql

    source 'employee' | search Gender='Male'
     | stats min_age=min(Age),max_age=max(Age) by MaritalStatus

Multiple fields

.. code-block:: pql

    source 'employee' |  stats min_age=min(Age),max_age=max(Age) by MaritalStatus,Gender

Supported Functions
^^^^^^^^^^^^^^^^^^^
* avg: calculates avg
* min: calculates min
* max: calculates max
* count: counts the number of values that are extracted from the aggregated documents
* percentiles: calculates percentiles
* sum: calculates sum
* cardinality: calculates an approximate count of distinct values


Date Histogram Aggregation
--------------------------
A multi-bucket aggregation similar to the histogram except it can only be applied on date values.
`Date histogram <https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket-datehistogram-aggregation.html#search-aggregations-bucket-datehistogram-aggregation>`_

.. code-block:: pql

    source 'employee' | search Gender='Male'
     | stats min_age=min(Age),max_age=max(Age) by MaritalStatus

Date histogram aggregation over a field

.. code-block:: pql

    source 'employee'
     | datehistogram field='DateOfJoining', interval='year' by Gender


Histogram Aggregation
---------------------
A multi-bucket values source based aggregation that can be applied on numeric values extracted from the documents. It dynamically builds fixed size (a.k.a. interval) buckets over the values.
`Date histogram <https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket-histogram-aggregation.html>`_

.. code-block:: pql

    source 'employee' | histogram field='Age', interval='10' by Gender

aggregation over field

.. code-block:: pql

    source 'employee' | histogram field='Age', interval='10' do max_age=max(Age) by Gender