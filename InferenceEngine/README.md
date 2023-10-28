Name: Khyati Gupta

Run the code HW.java -- it expects an input.txt

input.txt format:
1st line - the query
2nd line - number of line following (n)
n lines each a FoL statement that will be a part of the Knowledge Base (KB) used to infer the query

The code will convert the FoL statements into CNF form and create (and optimse) the KB.

FileReader will then use Resolution to see if the query can be inferred using the statements in the KB.
Will return False if cannot determine.
