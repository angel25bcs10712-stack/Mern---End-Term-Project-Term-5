INSERT INTO problem (id, title, description, difficulty, topic_id, external_url, tags, estimated_time_minutes, created_at, updated_at)

SELECT gen_random_uuid(), seed.title, seed.description, seed.difficulty, seed.topic_id::uuid, NULL,
       seed.tags, seed.estimated_time_minutes, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (VALUES
    -- Arrays: add 18, target 20
    ('Prefix Pivot Balance', 'Find every index where the sum before it equals the sum after it.', 'BEGINNER', '20000000-0000-0000-0000-000000000001', '["prefix sums", "array scan"]', 15),
    ('Stable Zero Transfer', 'Move all zero values to the end while keeping the order of nonzero values.', 'BEGINNER', '20000000-0000-0000-0000-000000000001', '["in-place", "two pointers"]', 15),
    ('Valley To Peak Streak', 'Return the longest contiguous run that strictly decreases and then strictly increases.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000001', '["array scan", "sequence"]', 25),
    ('Product Outside Index', 'Build an output where each position contains the product of all other input values.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000001', '["prefix products", "suffix products"]', 25),
    ('First Unclaimed Slot', 'Given occupied nonnegative labels, find the smallest label that is not present.', 'BEGINNER', '20000000-0000-0000-0000-000000000001', '["indexing", "array"]', 15),
    ('Maximum Uptime Run', 'Find the longest consecutive run of positive readings in a sensor array.', 'BEGINNER', '20000000-0000-0000-0000-000000000001', '["linear scan", "array"]', 15),
    ('Circular Shift Check', 'Determine whether one integer list is a cyclic rotation of another.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000001', '["rotation", "array"]', 20),
    ('Two Run Merge', 'Merge two already sorted integer sequences into one sorted result.', 'BEGINNER', '20000000-0000-0000-0000-000000000001', '["merge", "sorting"]', 20),
    ('Contiguous Gain Ledger', 'Return the largest sum obtainable from a nonempty contiguous section of daily changes.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000001', '["dynamic programming", "subarray"]', 25),
    ('Water Basin Volume', 'Compute how much water remains between bars of different heights after rainfall.', 'ADVANCED', '20000000-0000-0000-0000-000000000001', '["two pointers", "array"]', 35),
    ('Local Peak Count', 'Count positions whose value is strictly greater than each existing neighbor.', 'BEGINNER', '20000000-0000-0000-0000-000000000001', '["neighbors", "array"]', 15),
    ('Target Difference Pairs', 'Count index pairs whose values differ by a given nonnegative amount.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000001', '["sorting", "two pointers"]', 25),
    ('Compress Repeated Marks', 'Write each distinct value from a sorted list once at the front and return the new length.', 'BEGINNER', '20000000-0000-0000-0000-000000000001', '["in-place", "deduplication"]', 15),
    ('Rotation Boundary Index', 'Find the index of the smallest value in a rotated strictly increasing list.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000001', '["binary search", "rotation"]', 25),
    ('Three Signal Bands', 'Reorder values from three known categories so each category occupies one contiguous band.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000001', '["partitioning", "in-place"]', 25),
    ('Longest Rising Segment', 'Return the length of the longest contiguous strictly increasing section.', 'BEGINNER', '20000000-0000-0000-0000-000000000001', '["sequence", "linear scan"]', 15),
    ('Kth Sensor Reading', 'Find the kth largest value in an unsorted reading list without fully sorting it.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000001', '["selection", "heap"]', 30),
    ('Maximum Product Segment', 'Find the greatest product of any nonempty contiguous range, accounting for negative values.', 'ADVANCED', '20000000-0000-0000-0000-000000000001', '["dynamic programming", "subarray"]', 30),

    -- Strings: add 13, target 15
    ('Run Length Receipt', 'Encode consecutive repeated characters as a character followed by its run length.', 'BEGINNER', '20000000-0000-0000-0000-000000000002', '["encoding", "strings"]', 15),
    ('Remove Adjacent Noise', 'Repeatedly remove neighboring equal characters until no such pair remains.', 'BEGINNER', '20000000-0000-0000-0000-000000000002', '["stack", "strings"]', 20),
    ('Common Prefix Ticket', 'Return the longest prefix shared by every label in a list.', 'BEGINNER', '20000000-0000-0000-0000-000000000002', '["prefix", "strings"]', 15),
    ('First Repeated Marker', 'Return the earliest character whose second occurrence appears first in a scan.', 'BEGINNER', '20000000-0000-0000-0000-000000000002', '["character scan", "strings"]', 15),
    ('Mirror Letter Check', 'Check whether a phrase reads the same in both directions after case and punctuation are ignored.', 'BEGINNER', '20000000-0000-0000-0000-000000000002', '["palindrome", "normalization"]', 15),
    ('Delimiter Escape Audit', 'Validate quoted text where a backslash escapes the next character and quotes must pair.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000002', '["parsing", "state machine"]', 25),
    ('Smallest Rotation Label', 'Return the lexicographically smallest cyclic rotation of a nonempty string.', 'ADVANCED', '20000000-0000-0000-0000-000000000002', '["string matching", "rotation"]', 35),
    ('One Edit Budget', 'Determine whether two strings can be made equal with at most one insertion, deletion, or replacement.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000002', '["two pointers", "strings"]', 25),
    ('Longest Shared Run', 'Find the length of the longest substring that appears in both input strings.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000002', '["dynamic programming", "strings"]', 30),
    ('Token Frequency Summary', 'Split a line on whitespace and return each distinct token with its occurrence count.', 'BEGINNER', '20000000-0000-0000-0000-000000000002', '["tokenization", "counting"]', 15),
    ('Minimum Window Receipt', 'Find the shortest substring containing every required character with its required multiplicity.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000002', '["sliding window", "frequency"]', 30),
    ('Basic Formula Evaluator', 'Evaluate a string containing nonnegative integers, addition, subtraction, and parentheses.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000002', '["parsing", "stack"]', 30),
    ('Lexicographic Dispatch', 'Interleave two strings by repeatedly choosing the smaller available next character.', 'BEGINNER', '20000000-0000-0000-0000-000000000002', '["merge", "strings"]', 20),

    -- Hashing: add 13, target 15
    ('Seen Before Ledger', 'Return the first value encountered for a second time while scanning an integer list.', 'BEGINNER', '20000000-0000-0000-0000-000000000003', '["set", "array"]', 15),
    ('Pair Count Archive', 'Count distinct pairs of values whose sum equals a requested total.', 'BEGINNER', '20000000-0000-0000-0000-000000000003', '["frequency map", "pairs"]', 20),
    ('Most Frequent Code', 'Return the value with greatest frequency, breaking ties by choosing the smallest value.', 'BEGINNER', '20000000-0000-0000-0000-000000000003', '["frequency map", "array"]', 15),
    ('Longest Consecutive Shift', 'Find the length of the longest run of consecutive integer values regardless of input order.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000003', '["hash set", "sequence"]', 25),
    ('Subarray Sum Check', 'Determine whether any contiguous range sums to a requested target.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000003', '["prefix sums", "hash map"]', 25),
    ('Group Equivalent Labels', 'Group words that contain the same letters with identical frequencies.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000003', '["canonical key", "strings"]', 25),
    ('Unique Event Count', 'Count how many event identifiers occur exactly once in a stream.', 'BEGINNER', '20000000-0000-0000-0000-000000000003', '["frequency map", "counting"]', 15),
    ('First Complete Day', 'Find the earliest prefix of a log that has seen every identifier from a required set.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000003', '["set membership", "stream"]', 20),
    ('Zero Sum Segment', 'Count contiguous ranges whose values sum to zero.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000003', '["prefix sums", "hash map"]', 25),
    ('Collision Free Seats', 'Assign requested seat numbers while resolving occupied positions to the next free number.', 'BEGINNER', '20000000-0000-0000-0000-000000000003', '["hashing", "allocation"]', 20),
    ('Duplicate Receipt Total', 'Return the sum of values that appear more than once, counting each repeated value once.', 'BEGINNER', '20000000-0000-0000-0000-000000000003', '["set", "frequency map"]', 15),
    ('Equal Prefix Checkpoints', 'Find the longest prefix whose running sum matches a running sum at a later position.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000003', '["prefix sums", "hash map"]', 25),
    ('Anagram Bucket Index', 'Group words by a normalized character-count key and return the groups in input order.', 'ADVANCED', '20000000-0000-0000-0000-000000000003', '["hashing", "canonical form"]', 30),

    -- Two Pointers: add 8, target 10
    ('Closest Pair Sum', 'In a sorted list, find the pair sum with the smallest distance from a target.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000004', '["sorted input", "two pointers"]', 25),
    ('Container Span Score', 'Choose two positions whose height and distance produce the greatest contained area.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000004', '["two pointers", "greedy"]', 25),
    ('Unique Triplet Ledger', 'Return unique triples whose values sum to zero from an unsorted integer list.', 'ADVANCED', '20000000-0000-0000-0000-000000000004', '["sorting", "two pointers"]', 35),
    ('Crossed Pair Finder', 'Given two sorted arrays, find one value from each whose sum equals a target.', 'BEGINNER', '20000000-0000-0000-0000-000000000004', '["two pointers", "sorted input"]', 20),
    ('Remove Marked Range', 'Delete every value in a specified inclusive interval in place and return the retained length.', 'BEGINNER', '20000000-0000-0000-0000-000000000004', '["in-place", "two pointers"]', 15),
    ('Minimum Difference Pair', 'Choose one value from each sorted list to minimize their absolute difference.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000004', '["two pointers", "arrays"]', 20),
    ('Palindrome Pair Budget', 'Check whether a string can become a palindrome after removing at most one character.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000004', '["two pointers", "strings"]', 20),
    ('Three Color Partition', 'Partition an array of three category codes into category order in one pass.', 'BEGINNER', '20000000-0000-0000-0000-000000000004', '["partitioning", "in-place"]', 20),

    -- Sliding Window: add 8, target 10
    ('Average Above Threshold', 'Count fixed-size contiguous windows whose average is greater than a given limit.', 'BEGINNER', '20000000-0000-0000-0000-000000000005', '["fixed window", "array"]', 20),
    ('Longest Budgeted Run', 'Find the longest contiguous range whose sum does not exceed a nonnegative budget.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000005', '["variable window", "array"]', 25),
    ('Distinct Values Per Window', 'Return the number of distinct values in every fixed-length window.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000005', '["sliding window", "frequency map"]', 25),
    ('Longest Two Category Run', 'Find the longest contiguous section containing no more than two distinct categories.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000005', '["variable window", "hash map"]', 25),
    ('Minimum Positive Window', 'Find the shortest contiguous range whose sum is at least a requested positive value.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000005', '["variable window", "positive values"]', 25),
    ('Windowed Peak Alert', 'For each fixed-size window, report its maximum reading.', 'BEGINNER', '20000000-0000-0000-0000-000000000005', '["deque", "sliding window"]', 25),
    ('Replaceable Signal Span', 'Find the longest substring that can be made uniform with at most k replacements.', 'ADVANCED', '20000000-0000-0000-0000-000000000005', '["sliding window", "strings"]', 30),
    ('Binary Run With Repairs', 'Find the longest substring of ones after changing at most k zeroes.', 'BEGINNER', '20000000-0000-0000-0000-000000000005', '["sliding window", "binary array"]', 20),

    -- Stack: add 8, target 10
    ('Daily Warmer Forecast', 'For each day, report how many days pass before a warmer reading occurs.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000006', '["monotonic stack", "array"]', 25),
    ('Histogram Skyline Area', 'Find the largest rectangle that fits beneath a histogram of bar heights.', 'ADVANCED', '20000000-0000-0000-0000-000000000006', '["monotonic stack", "histogram"]', 35),
    ('Simplify Route Path', 'Normalize a slash-separated path by resolving current and parent directory markers.', 'BEGINNER', '20000000-0000-0000-0000-000000000006', '["stack", "path parsing"]', 20),
    ('Undoable Text Editor', 'Process typed characters and backspace markers to produce the final text.', 'BEGINNER', '20000000-0000-0000-0000-000000000006', '["stack", "strings"]', 15),
    ('Nested Score Parser', 'Evaluate a nested expression where matched brackets multiply the enclosed score.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000006', '["stack", "parsing"]', 25),
    ('Stock Span Record', 'For each price, count consecutive earlier prices that are no greater than it.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000006', '["monotonic stack", "stream"]', 25),
    ('Next Greater Circular Read', 'For each value in a circular list, find its next strictly greater value.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000006', '["monotonic stack", "circular array"]', 25),
    ('Stack With Constant Minimum', 'Design a stack that returns its current minimum in constant time alongside push and pop.', 'BEGINNER', '20000000-0000-0000-0000-000000000006', '["stack design", "minimum"]', 20),

    -- Queue: add 6, target 8
    ('Round Robin Workload', 'Simulate time slices for queued jobs and return each job completion time.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000007', '["queue", "simulation"]', 25),
    ('Recent Request Gate', 'Design a request counter that reports events occurring within the latest time interval.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000007', '["queue", "time window"]', 25),
    ('Two Queue Stack', 'Implement last-in-first-out push, pop, and top operations using two queues.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000007', '["queue", "stack"]', 25),
    ('Alternating Service Lines', 'Merge two arrival queues by taking one item from each in turn until both are empty.', 'BEGINNER', '20000000-0000-0000-0000-000000000007', '["queue", "simulation"]', 15),
    ('Rotating Task Queue', 'Process a task only when its priority is maximal among queued tasks, rotating others to the back.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000007', '["queue", "priority"]', 25),
    ('First Unique Stream Item', 'After each arriving character, report the earliest character seen exactly once so far.', 'BEGINNER', '20000000-0000-0000-0000-000000000007', '["queue", "frequency"]', 20),

    -- Linked List: add 10, target 12
    ('Merge Ordered Chains', 'Merge two sorted singly linked lists without creating replacement data nodes.', 'BEGINNER', '20000000-0000-0000-0000-000000000008', '["merge", "linked list"]', 20),
    ('Remove From Tail Offset', 'Remove the node at a given positive offset from the end using one list pass.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000008', '["two pointers", "linked list"]', 25),
    ('Reorder Around Center', 'Rearrange nodes as first, last, second, second-last, continuing inward.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000008', '["linked list", "in-place"]', 30),
    ('Add Reversed Digit Chains', 'Add two nonnegative integers represented by reverse-order digit nodes.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000008', '["linked list", "carry"]', 25),
    ('Sort Chain By Values', 'Sort a singly linked list in ascending order with logarithmic recursion depth.', 'ADVANCED', '20000000-0000-0000-0000-000000000008', '["merge sort", "linked list"]', 35),
    ('Split Into Balanced Chains', 'Split a linked list into k consecutive parts whose sizes differ by at most one.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000008', '["linked list", "partitioning"]', 25),
    ('Rotate Chain Right', 'Move the final k nodes of a singly linked list to its front.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000008', '["linked list", "rotation"]', 25),
    ('Remove Sorted Chain Duplicates', 'Keep one node for each value in a sorted linked list.', 'BEGINNER', '20000000-0000-0000-0000-000000000008', '["linked list", "deduplication"]', 15),
    ('Intersection Node Finder', 'Return the first shared node of two singly linked chains, if they intersect.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000008', '["two pointers", "linked list"]', 25),
    ('Copy Random Reference Chain', 'Deep-copy a linked structure where each node also points to an arbitrary node.', 'ADVANCED', '20000000-0000-0000-0000-000000000008', '["hash map", "linked list"]', 35),

    -- Binary Search: add 10, target 12
    ('First Value At Least Target', 'Return the first index whose sorted value is no smaller than a target.', 'BEGINNER', '20000000-0000-0000-0000-000000000009', '["lower bound", "binary search"]', 15),
    ('Last Value At Most Target', 'Return the final index whose sorted value is no greater than a target.', 'BEGINNER', '20000000-0000-0000-0000-000000000009', '["upper bound", "binary search"]', 15),
    ('Search Rotated With Repeats', 'Search a rotated sorted list that may contain duplicate values.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000009', '["binary search", "duplicates"]', 25),
    ('Peak In Mountain Range', 'Find the peak index in a sequence that strictly rises then strictly falls.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000009', '["binary search", "peak"]', 20),
    ('Integer Root Floor', 'Return the greatest integer whose square does not exceed a nonnegative input.', 'BEGINNER', '20000000-0000-0000-0000-000000000009', '["binary search", "numeric"]', 15),
    ('Minimum Shipping Capacity', 'Find the smallest daily capacity that ships ordered packages within a fixed number of days.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000009', '["binary search", "answer space"]', 30),
    ('Median Of Sorted Streams', 'Find the median of two sorted arrays in logarithmic time relative to the shorter array.', 'ADVANCED', '20000000-0000-0000-0000-000000000009', '["binary search", "partition"]', 40),
    ('Minimum Rotated Value', 'Find the smallest value in a rotated strictly increasing list.', 'BEGINNER', '20000000-0000-0000-0000-000000000009', '["binary search", "rotation"]', 20),
    ('Exact Range Boundaries', 'Return the first and last indices of a target value in a sorted list.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000009', '["binary search", "bounds"]', 25),
    ('Rate To Finish On Time', 'Find the minimum integer processing rate that completes all workloads within a deadline.', 'INTERMEDIATE', '20000000-0000-0000-0000-000000000009', '["binary search", "answer space"]', 30),

    -- Trees: add 13, target 15
    ('Tree Height Report', 'Return the number of edges on the longest path from the root to a leaf.', 'BEGINNER', '20000000-0000-0000-0000-00000000000a', '["depth-first search", "tree"]', 20),
    ('Mirror Shape Check', 'Determine whether a binary tree is symmetric around its root.', 'BEGINNER', '20000000-0000-0000-0000-00000000000a', '["recursion", "tree"]', 20),
    ('Lowest Shared Ancestor', 'Find the deepest node that is an ancestor of two given nodes in a binary search tree.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000a', '["tree", "binary search tree"]', 25),
    ('Root To Leaf Target Paths', 'List root-to-leaf value paths whose values sum to a requested target.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000a', '["depth-first search", "backtracking"]', 25),
    ('Tree Diameter Length', 'Find the greatest number of edges on a path between any two tree nodes.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000a', '["tree", "postorder"]', 25),
    ('Sorted List To Search Tree', 'Build a height-balanced binary search tree from a sorted linked list.', 'ADVANCED', '20000000-0000-0000-0000-00000000000a', '["tree construction", "linked list"]', 40),
    ('Column Grouped Traversal', 'Group binary tree node values by vertical column from left to right.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000a', '["breadth-first search", "tree"]', 30),
    ('Nearest Tree Leaf', 'Find the shortest edge distance from a target node to any leaf.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000a', '["breadth-first search", "tree"]', 30),
    ('Flatten Tree To Chain', 'Rewire a binary tree in place into preorder right-child links.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000a', '["tree", "in-place"]', 25),
    ('Recover Swapped Tree Keys', 'Restore a binary search tree where exactly two node values were exchanged.', 'ADVANCED', '20000000-0000-0000-0000-00000000000a', '["inorder traversal", "tree"]', 35),
    ('Complete Tree Node Count', 'Count nodes in a complete binary tree faster than visiting every node.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000a', '["tree", "binary search"]', 25),
    ('Subtree Match Finder', 'Check whether one binary tree occurs as a rooted subtree of another.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000a', '["tree", "serialization"]', 25),
    ('Maximum Path Value', 'Find the greatest sum along any nonempty path in a binary tree.', 'ADVANCED', '20000000-0000-0000-0000-00000000000a', '["tree", "postorder"]', 35),

    -- Graphs: add 13, target 15
    ('Course Order Planner', 'Return a valid order for tasks with prerequisite edges, or an empty result when impossible.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000b', '["topological sort", "directed graph"]', 30),
    ('Island Region Count', 'Count connected land regions in a rectangular grid using horizontal and vertical adjacency.', 'BEGINNER', '20000000-0000-0000-0000-00000000000b', '["depth-first search", "grid"]', 20),
    ('Clone Contact Network', 'Create an independent copy of a connected graph with arbitrary neighbor links.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000b', '["breadth-first search", "hash map"]', 25),
    ('Weighted Route Minimum', 'Find the least total edge weight between two vertices with nonnegative edge costs.', 'ADVANCED', '20000000-0000-0000-0000-00000000000b', '["dijkstra", "priority queue"]', 35),
    ('Bridge Link Detector', 'Find graph edges whose removal would increase the number of connected components.', 'ADVANCED', '20000000-0000-0000-0000-00000000000b', '["depth-first search", "low link"]', 40),
    ('Bipartite Team Split', 'Determine whether an undirected graph can be colored with two colors so adjacent nodes differ.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000b', '["breadth-first search", "coloring"]', 25),
    ('Grid Exit Distance', 'Find the fewest moves from a start cell to any open boundary cell in a maze grid.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000b', '["breadth-first search", "grid"]', 25),
    ('Minimum Network Cable', 'Connect every site at minimum total cost using a subset of weighted undirected links.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000b', '["minimum spanning tree", "union find"]', 35),
    ('Redundant Link Report', 'Identify an edge that can be removed while keeping an undirected network connected.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000b', '["union find", "cycle detection"]', 25),
    ('Word Ladder Steps', 'Find the shortest sequence of dictionary words connecting two words by one-letter changes.', 'ADVANCED', '20000000-0000-0000-0000-00000000000b', '["breadth-first search", "strings"]', 35),
    ('Safe City Reachability', 'Mark grid cells that can reach both of two ocean borders by moving only to equal or higher ground.', 'ADVANCED', '20000000-0000-0000-0000-00000000000b', '["graph traversal", "matrix"]', 40),
    ('Dependency Cycle Check', 'Determine whether a directed dependency network contains a cycle.', 'BEGINNER', '20000000-0000-0000-0000-00000000000b', '["cycle detection", "graph"]', 20),
    ('Connected Pair Suggestions', 'For each person, count how many non-neighbors share at least one direct contact.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000b', '["graph", "sets"]', 30),

    -- Greedy: add 6, target 8
    ('Fewest Coin Types', 'Find the minimum number of available coin values needed to reach an amount when the system is canonical.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000c', '["greedy", "coins"]', 25),
    ('Maximum Compatible Events', 'Select the largest set of non-overlapping events from their start and finish times.', 'BEGINNER', '20000000-0000-0000-0000-00000000000c', '["sorting", "intervals"]', 20),
    ('Minimum Arrow Bursts', 'Cover every closed interval with the fewest points chosen on the number line.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000c', '["greedy", "intervals"]', 25),
    ('Assign Fair Cookies', 'Maximize satisfied children by assigning each child at most one cookie meeting their size need.', 'BEGINNER', '20000000-0000-0000-0000-00000000000c', '["sorting", "greedy"]', 20),
    ('Minimum Jump Count', 'Find the fewest forward jumps needed to reach the end when each position sets a jump limit.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000c', '["greedy", "array"]', 25),
    ('Refuel With Largest Reach', 'Reach a destination with the fewest stops by selecting the best available fuel station so far.', 'ADVANCED', '20000000-0000-0000-0000-00000000000c', '["greedy", "priority queue"]', 35),

    -- Backtracking: add 3, target 5
    ('Distinct Word Arrangements', 'Generate every unique ordering of a string that may contain repeated characters.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000d', '["backtracking", "permutations"]', 30),
    ('Place Nonattacking Rooks', 'Count placements of k rooks on an obstacle grid so no two share a row or column.', 'ADVANCED', '20000000-0000-0000-0000-00000000000d', '["backtracking", "constraints"]', 40),
    ('Word Path In Board', 'Find whether a word can be traced through adjacent board cells without reusing a cell.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000d', '["depth-first search", "backtracking"]', 30),

    -- Dynamic Programming: add 13, target 15
    ('Ways To Reach Landing', 'Count distinct ways to reach step n when each move advances one, two, or three steps.', 'BEGINNER', '20000000-0000-0000-0000-00000000000e', '["dynamic programming", "counting"]', 20),
    ('Minimum Coin Total', 'Find the fewest coins needed to make an amount when each denomination can be reused.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000e', '["dynamic programming", "coins"]', 25),
    ('Longest Rising Subsequence', 'Return the length of the longest strictly increasing subsequence, not necessarily contiguous.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000e', '["dynamic programming", "sequence"]', 30),
    ('Minimum Grid Travel Cost', 'Find the least sum along a path from the top-left to bottom-right using only right and down moves.', 'BEGINNER', '20000000-0000-0000-0000-00000000000e', '["dynamic programming", "grid"]', 20),
    ('Equal Partition Possible', 'Determine whether a list can be divided into two subsets with equal total sum.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000e', '["dynamic programming", "subsets"]', 30),
    ('Longest Palindrome Subsequence', 'Find the length of the longest subsequence that reads identically in both directions.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000e', '["dynamic programming", "strings"]', 30),
    ('Minimum Edit Operations', 'Find the fewest insertions, removals, or replacements needed to transform one word into another.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000e', '["dynamic programming", "strings"]', 30),
    ('Burst Interval Reward', 'Choose an order to remove numbered items where each removal reward depends on current neighbors.', 'ADVANCED', '20000000-0000-0000-0000-00000000000e', '["interval dynamic programming", "optimization"]', 45),
    ('Two Worker Schedule', 'Maximize completed task value when two workers cannot take adjacent tasks from the schedule.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000e', '["dynamic programming", "state"]', 30),
    ('Decode Number Message', 'Count valid letter decodings of a digit string where one or two digits form a letter code.', 'BEGINNER', '20000000-0000-0000-0000-00000000000e', '["dynamic programming", "strings"]', 20),
    ('Best Two Trade Profit', 'Find the greatest gain from at most two buy-and-sell transactions in a price sequence.', 'ADVANCED', '20000000-0000-0000-0000-00000000000e', '["dynamic programming", "state machine"]', 40),
    ('Target Signed Expression Count', 'Count ways to assign plus or minus signs to values so their total equals a target.', 'INTERMEDIATE', '20000000-0000-0000-0000-00000000000e', '["dynamic programming", "subset sum"]', 30),
    ('Minimum Palindrome Cuts', 'Split a string into the fewest palindromic pieces possible.', 'ADVANCED', '20000000-0000-0000-0000-00000000000e', '["dynamic programming", "strings"]', 40)
) AS seed(title, description, difficulty, topic_id, tags, estimated_time_minutes)
WHERE NOT EXISTS (
	SELECT 1 FROM problem existing WHERE lower(existing.title) = lower(seed.title)
);
