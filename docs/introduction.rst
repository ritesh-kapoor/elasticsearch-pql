Introduction
============

With this plugin you can query elasticsearch using pipeline query syntax.

A search consists of a series of commands that are delimited by pipe ( | ) characters. The first whitespace-delimited string after each pipe character controls the command used.

PQL - Pipeline Query Syntax
---------------------------


The anatomy of a search
^^^^^^^^^^^^^^^^^^^^^^^

.. code-block:: pql

    source statements
        | [search statements [... search statements]]
             [| stats statements [| bucket statements]]


Sample Query
^^^^^^^^^^^^

.. code-block:: pql

    source 'employee' | search Gender='Male' OR MaritalStatus='Unmarried'

Example
-------
.. code-block:: bash

    curl -XGET -G "http://localhost:9200/_pql" --data-urlencode "query=source 'employee*' | search Gender='Male'"


Contribute on GitHub
--------------------
You may also wish to follow the GitHub project if you have a GitHub account.
This is also where we keep the issue tracker for sharing bugs and feature ideas.
`Github <https://github.com/ritesh-kapoor/elasticsearch-pql>`_
