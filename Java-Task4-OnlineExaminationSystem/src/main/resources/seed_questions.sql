-- 25 Accurate, high-quality questions across 6 categories for ExamPortal

INSERT INTO questions (category, question_text, option_a, option_b, option_c, option_d, correct_option) VALUES
-- Java Fundamentals
('Java Fundamentals', 'Which memory area in the JVM is primarily responsible for storing object instances and where Garbage Collection occurs?', 'Method Area', 'Heap Memory', 'Java Call Stack', 'Native Method Stack', 'B'),
('Java Fundamentals', 'Which of the following is NOT a primitive data type in Java?', 'boolean', 'int', 'String', 'double', 'C'),
('Java Fundamentals', 'Why are String objects considered immutable in Java?', 'Their internal char/byte array cannot be modified once created', 'They cannot be used as keys in a Map', 'They are automatically marked as final classes', 'They reside only in the Stack memory', 'A'),
('Java Fundamentals', 'What happens when a variable in Java is declared with the keyword "final"?', 'It cannot be inherited by subclasses', 'It cannot be reassigned once initialized', 'It can only be accessed within its package', 'It is automatically serialized', 'B'),
('Java Fundamentals', 'What is the output format produced by the standard Java compiler (javac)?', 'Assembly language machine code', 'Native operating system binaries', 'Java Virtual Machine Bytecode (.class)', 'Intermediate C++ code', 'C'),

-- OOP (Object-Oriented Programming)
('OOP', 'Which concept allows a subclass to provide a specific implementation of a method already defined in its superclass?', 'Method Overloading', 'Method Overriding', 'Data Encapsulation', 'Multiple Inheritance', 'B'),
('OOP', 'Which statement accurately describes polymorphism in Java?', 'A class can inherit fields from multiple concrete parent classes', 'An object can take on many forms through inheritance and interface implementation', 'All methods in an abstract class must be declared static', 'Encapsulating data prevents methods from accessing variables', 'B'),
('OOP', 'What is the primary objective of data encapsulation in Object-Oriented Programming?', 'To combine unrelated classes into a single package', 'To bundle data and methods together while restricting direct access from outside', 'To execute methods asynchronously on background threads', 'To enforce compile-time type conversion', 'B'),
('OOP', 'In modern Java (Java 8 and above), what feature can interfaces provide that traditional interfaces could not?', 'Instance variables that store state', 'Default and static method implementations', 'Private constructors', 'Final concrete classes', 'B'),
('OOP', 'Which of the following is true regarding constructors in Java?', 'Constructors have a void return type', 'Constructors cannot be overloaded', 'Constructors cannot be declared final, static, or abstract', 'Constructors are automatically inherited by subclasses', 'C'),

-- Data Structures
('Data Structures', 'What is the average-case time complexity of look-up and insertion in a well-distributed HashMap?', 'O(n)', 'O(log n)', 'O(1)', 'O(n log n)', 'C'),
('Data Structures', 'Which Java List implementation provides O(1) constant-time positional access to elements via index?', 'LinkedList', 'ArrayList', 'TreeSet', 'LinkedHashSet', 'B'),
('Data Structures', 'Which data structure follows the First-In, First-Out (FIFO) principle?', 'Stack', 'Queue', 'Binary Search Tree', 'Max Heap', 'B'),
('Data Structures', 'In a Binary Search Tree (BST), which tree traversal order visits nodes in strictly ascending numerical order?', 'Pre-order Traversal', 'Post-order Traversal', 'In-order Traversal', 'Level-order Traversal', 'C'),
('Data Structures', 'What underlying data structure is typically used by Java''s java.util.PriorityQueue?', 'Circular Doubly-Linked List', 'Self-Balancing Red-Black Tree', 'Binary Heap array', 'Hash Table with chaining', 'C'),

-- Databases & SQL
('Databases & SQL', 'In the ACID model of relational database transactions, what does "A" stand for?', 'Availability', 'Atomicity', 'Authentication', 'Authorization', 'B'),
('Databases & SQL', 'Which constraint ensures that a database table column uniquely identifies each row and never contains NULL?', 'UNIQUE constraint', 'FOREIGN KEY constraint', 'PRIMARY KEY constraint', 'CHECK constraint', 'C'),
('Databases & SQL', 'What is the difference between SQL WHERE and HAVING clauses?', 'WHERE filters before grouping - HAVING filters after GROUP BY aggregation', 'WHERE applies only to subqueries - HAVING applies to table joins', 'HAVING can only compare strings - WHERE compares numbers', 'There is no functional difference - they are interchangeable', 'A'),
('Databases & SQL', 'Which SQL join returns all rows from the left table, and matching rows from the right table (filling NULL for non-matches)?', 'INNER JOIN', 'LEFT OUTER JOIN', 'FULL JOIN', 'CROSS JOIN', 'B'),
('Databases & SQL', 'Why do relational databases use indexes on frequently queried columns?', 'To compress table data and reduce hard disk space', 'To speed up data retrieval operations at the trade-off of additional write/storage overhead', 'To guarantee that no duplicate data can ever be inserted', 'To automatically generate foreign key relationships', 'B'),

-- Networking
('Networking', 'Which protocol provides reliable, ordered, and error-checked delivery of a stream of packets?', 'User Datagram Protocol (UDP)', 'Transmission Control Protocol (TCP)', 'Internet Control Message Protocol (ICMP)', 'Address Resolution Protocol (ARP)', 'B'),
('Networking', 'What does the HTTP 404 response status code represent?', 'Internal Server Error', 'Unauthorized Access', 'Resource Not Found', 'Bad Request', 'C'),
('Networking', 'What are the default standard port numbers for unencrypted HTTP and encrypted HTTPS respectively?', '21 and 22', '80 and 443', '8080 and 8443', '25 and 587', 'B'),
('Networking', 'What is the fundamental role of the Domain Name System (DNS)?', 'To encrypt web traffic using SSL/TLS certificates', 'To translate human-friendly domain names (e.g. example.com) into numerical IP addresses', 'To balance network traffic across web server clusters', 'To route physical Ethernet packets across local switches', 'B'),

-- General CS
('General CS', 'What is the worst-case time complexity of the Merge Sort algorithm for an array of size n?', 'O(n^2)', 'O(n log n)', 'O(n)', 'O(log n)', 'B');
