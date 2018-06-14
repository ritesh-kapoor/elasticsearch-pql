Bucket Statement
================

.. toctree::
   :maxdepth: 2
   :numbered:

.. contents:: Table of Contents
   :depth: 2
   :local:

Bucket Selection
----------------
A parent pipeline aggregation which executes a script which determines whether the current bucket will be retained in the parent multi-bucket aggregation.

.. code-block:: pql

    source 'employee'
     | stats min_age=min(Age),max_age=max(Age) by MaritalStatus,Gender
     | bucket select min_age=44.0 AND max_age=65.0

.. code-block:: pql

    source 'employee'
     | datehistogram field='DateOfJoining', interval='year' do min_age=min(Age)
     | bucket stats derivative_age=derivative(min_age)
