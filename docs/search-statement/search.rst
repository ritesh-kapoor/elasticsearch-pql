Search Statement
================

.. toctree::
   :maxdepth: 2
   :numbered:

.. contents:: Table of Contents
   :depth: 2
   :local:

Boolean Statements
------------------

The and Statement
^^^^^^^^^^^^^^^^^

The clause (query) must appear in matching documents

.. code-block:: pql

    source 'employee' | search Gender='Male' AND MaritalStatus='Unmarried'

The or Statement
^^^^^^^^^^^^^^^^

The clause (query) should appear in matching documents.

.. code-block:: pql

    source 'employee' | search Gender='Male' OR MaritalStatus='Unmarried'


The equals Statement
--------------------

The equals query is equivalent to term query and finds documents that contain the exact term specified in the inverted index. For instance:

.. code-block:: pql

    source 'employee' | search Gender='Male'


The not equals Statement
------------------------

The clause (query) should not appear in matching documents.

.. code-block:: pql

    source 'employee' | search LastName!='RECHKEMMER'


Regex Statement
---------------

The regexp query allows you to use regular expression term queries.

.. code-block:: pql

    source 'employee' | search regex MaritalStatus='Marr.*'

Wildcard Statement
------------------

Matches documents that have fields matching a wildcard expression (not analyzed).
Supported wildcards are *, which matches any character sequence (including the empty one), and ?, which matches any single character. Link_.

.. _Link: https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-wildcard-query.html


.. code-block:: pql

    source 'employee' | search wildcard MaritalStatus='Marr*'


Exact match Statement
---------------------

The query finds documents that contain the exact term specified in the inverted index.
`Term query <https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-term-query.html>`_


.. code-block:: pql

    source 'employee' | search Gender==='Male'

Range Statement
---------------

Matches documents with fields that have terms within a certain range.

.. code-block:: pql

    source 'employee' | search Salary>100000 && Salary<103000


Eval Statement
--------------

A query allowing to define scripts as queries.
`Script query <https://www.elastic.co/guide/en/elasticsearch/reference/5.4/query-dsl-script-query.html#query-dsl-script-query>`_


.. code-block:: pql

    source 'employee' | eval NewSalary='return 9000+doc["Salary"].value',NewSalary2='return 9000+doc["Salary"].value'



From / Size
-----------

Pagination of results can be done by using the limit query.

.. note::  limit <from>,<size>

.. code-block:: pql

    source 'employee' | limit 1,2


Sorting
-------
Allows to add one or more sort on specific fields. Each sort can be reversed as well.

.. code-block:: pql

    source 'employee' | sort Gender Desc,MaritalStatus ASC


