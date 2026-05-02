# excel4j Formula Function Reference

All 98 built-in functions supported by `excel4j-formula`. Functions are case-insensitive.

To evaluate formulas, call `workbook.recalculate(evaluator::evaluate)` after setting up a `FormulaEvaluator`.

---

## Table of Contents

- [Math & Statistics](#math--statistics)
- [Logic & Information](#logic--information)
- [Text](#text)
- [Date & Time](#date--time)
- [Lookup & Reference](#lookup--reference)
- [Array](#array)
- [Charts](#charts)
- [Custom Functions](#custom-functions)
- [Error Types](#error-types)

---

## Math & Statistics

### SUM

```
SUM(number1, [number2, ...])
```

Returns the sum of all numeric arguments. Booleans count as 1 (TRUE) or 0 (FALSE). Blanks are treated as 0. Propagates errors.

```
=SUM(A1:A10)       → sum of range
=SUM(1, 2, 3)      → 6
=SUM(A1, B1, 5)    → A1 + B1 + 5
```

---

### PRODUCT

```
PRODUCT(number1, [number2, ...])
```

Returns the product of all numeric arguments. Booleans count as 1 or 0. Propagates errors.

```
=PRODUCT(2, 3, 4)  → 24
=PRODUCT(A1:A3)    → A1 * A2 * A3
```

---

### MIN

```
MIN(number1, [number2, ...])
```

Returns the smallest numeric value. Ignores text and blanks. Returns 0 if no numeric values found.

```
=MIN(3, 1, 4, 1, 5)  → 1
=MIN(A1:A10)          → smallest value in range
```

---

### MAX

```
MAX(number1, [number2, ...])
```

Returns the largest numeric value. Ignores text and blanks. Returns 0 if no numeric values found.

```
=MAX(3, 1, 4, 1, 5)  → 5
=MAX(A1:A10)          → largest value in range
```

---

### AVERAGE

```
AVERAGE(number1, [number2, ...])
```

Returns the arithmetic mean of all numeric arguments. Blanks are skipped. Returns `#DIV/0!` if no numeric values.

```
=AVERAGE(1, 2, 3)   → 2
=AVERAGE(A1:A10)    → mean of range
```

---

### COUNT

```
COUNT(value1, [value2, ...])
```

Counts the number of numeric or boolean values in the arguments. Text and blanks are not counted.

```
=COUNT(1, "a", 2, TRUE)  → 3
=COUNT(A1:A10)            → number of numeric cells
```

---

### ABS

```
ABS(number)
```

Returns the absolute value of a number.

```
=ABS(-5)   → 5
=ABS(3)    → 3
```

---

### ROUND

```
ROUND(number, digits)
```

Rounds a number to the specified number of decimal places. Negative `digits` rounds left of the decimal point.

```
=ROUND(3.14159, 2)   → 3.14
=ROUND(1234, -2)     → 1200
```

---

### INT

```
INT(number)
```

Rounds a number down to the nearest integer (floor).

```
=INT(3.9)    → 3
=INT(-3.1)   → -4
```

---

### MOD

```
MOD(number, divisor)
```

Returns the remainder after dividing `number` by `divisor`. Returns `#DIV/0!` if divisor is 0.

```
=MOD(10, 3)   → 1
=MOD(7, 2)    → 1
```

---

### POWER

```
POWER(number, power)
```

Returns `number` raised to `power`.

```
=POWER(2, 10)   → 1024
=POWER(9, 0.5)  → 3
```

---

### SQRT

```
SQRT(number)
```

Returns the square root of a non-negative number. Returns `#NUM!` for negative input.

```
=SQRT(16)    → 4
=SQRT(2)     → 1.4142...
=SQRT(-1)    → #NUM!
```

---

### CEILING

```
CEILING(number, significance)
```

Rounds a number up to the nearest multiple of `significance`.

```
=CEILING(2.3, 1)    → 3
=CEILING(2.3, 0.5)  → 2.5
=CEILING(6.7, 2)    → 8
```

---

### FLOOR

```
FLOOR(number, significance)
```

Rounds a number down to the nearest multiple of `significance`.

```
=FLOOR(2.7, 1)    → 2
=FLOOR(2.7, 0.5)  → 2.5
=FLOOR(6.7, 2)    → 6
```

---

### ROUNDUP

```
ROUNDUP(number, num_digits)
```

Rounds a number away from zero to the given number of decimal places.

```
=ROUNDUP(2.1, 0)   → 3
=ROUNDUP(-2.1, 0)  → -3
=ROUNDUP(2.14, 1)  → 2.2
```

---

### ROUNDDOWN

```
ROUNDDOWN(number, num_digits)
```

Rounds a number toward zero to the given number of decimal places.

```
=ROUNDDOWN(2.9, 0)   → 2
=ROUNDDOWN(-2.9, 0)  → -2
=ROUNDDOWN(2.99, 1)  → 2.9
```

---

### TRUNC

```
TRUNC(number, [num_digits])
```

Truncates a number to the given number of decimal places (default 0) by removing trailing digits without rounding.

```
=TRUNC(3.7)       → 3
=TRUNC(-3.7)      → -3
=TRUNC(3.789, 2)  → 3.78
```

---

### SIGN

```
SIGN(number)
```

Returns `1` if the number is positive, `0` if zero, `-1` if negative.

```
=SIGN(5)   → 1
=SIGN(0)   → 0
=SIGN(-5)  → -1
```

---

### LOG

```
LOG(number, [base])
```

Returns the logarithm of a number to the specified base. Default base is 10.

```
=LOG(100, 10)  → 2
=LOG(10)       → 1
=LOG(8, 2)     → 3
```

---

### LOG10

```
LOG10(number)
```

Returns the base-10 logarithm of a number.

```
=LOG10(1000)  → 3
=LOG10(100)   → 2
```

---

### LN

```
LN(number)
```

Returns the natural (base-e) logarithm of a number.

```
=LN(EXP(1))  → 1
=LN(1)       → 0
```

---

### EXP

```
EXP(number)
```

Returns `e` raised to the power of `number`.

```
=EXP(1)  → 2.71828...
=EXP(0)  → 1
```

---

### PI

```
PI()
```

Returns the value of π (3.14159...).

```
=PI()              → 3.14159265358979...
=2 * PI() * A1     (circumference where A1 is radius)
```

---

### RAND

```
RAND()
```

Returns a random decimal number between 0 (inclusive) and 1 (exclusive). Recalculates on every evaluation.

```
=RAND()          → 0.47291...  (example)
=RAND() * 100    → random 0–100
```

---

### RANDBETWEEN

```
RANDBETWEEN(bottom, top)
```

Returns a random integer between `bottom` and `top` (inclusive).

```
=RANDBETWEEN(1, 6)    → random die roll
=RANDBETWEEN(1, 100)  → random integer 1–100
```

---

### FACT

```
FACT(number)
```

Returns the factorial of a non-negative integer. Returns `#NUM!` for negative input.

```
=FACT(5)  → 120
=FACT(0)  → 1
=FACT(10) → 3628800
```

---

### SUMPRODUCT

```
SUMPRODUCT(array1, [array2, ...])
```

Returns the sum of products of corresponding elements. With the flat-args evaluator, single-range usage is equivalent to `SUM`.

```
=SUMPRODUCT(A1:A3)           → sum of A1:A3
=SUMPRODUCT(2, 3, 4)         → 9  (2+3+4)
```

---

### SUMSQ

```
SUMSQ(number1, [number2, ...])
```

Returns the sum of the squares of its arguments.

```
=SUMSQ(3, 4)     → 25  (9 + 16)
=SUMSQ(1, 2, 3)  → 14  (1 + 4 + 9)
```

---

## Logic & Information

### IF

```
IF(condition, value_if_true, [value_if_false])
```

Returns one value if condition is TRUE, another if FALSE. `value_if_false` defaults to FALSE if omitted.

```
=IF(A1>10, "High", "Low")
=IF(ISBLANK(A1), "Empty", A1)
```

---

### AND

```
AND(logical1, [logical2, ...])
```

Returns TRUE if all arguments are TRUE. Returns FALSE as soon as any argument is FALSE.

```
=AND(A1>0, B1>0)        → TRUE only if both positive
=AND(TRUE, TRUE, FALSE) → FALSE
```

---

### OR

```
OR(logical1, [logical2, ...])
```

Returns TRUE if any argument is TRUE. Returns FALSE only if all arguments are FALSE.

```
=OR(A1>0, B1>0)         → TRUE if either positive
=OR(FALSE, FALSE, TRUE) → TRUE
```

---

### NOT

```
NOT(logical)
```

Reverses the logical value of its argument.

```
=NOT(TRUE)    → FALSE
=NOT(A1>10)   → TRUE when A1 <= 10
```

---

### IFERROR

```
IFERROR(value, value_if_error)
```

Returns `value_if_error` if `value` evaluates to any error; otherwise returns `value`.

```
=IFERROR(A1/B1, 0)          → 0 instead of #DIV/0!
=IFERROR(VLOOKUP(...), "?") → "?" when not found
```

---

### ISBLANK

```
ISBLANK(value)
```

Returns TRUE if the cell is empty (blank).

```
=ISBLANK(A1)   → TRUE if A1 is empty
```

---

### ISNUMBER

```
ISNUMBER(value)
```

Returns TRUE if the value is a number.

```
=ISNUMBER(42)     → TRUE
=ISNUMBER("42")   → FALSE
```

---

### ISTEXT

```
ISTEXT(value)
```

Returns TRUE if the value is text.

```
=ISTEXT("hello")  → TRUE
=ISTEXT(123)      → FALSE
```

---

### ISLOGICAL

```
ISLOGICAL(value)
```

Returns TRUE if the value is a boolean (TRUE or FALSE).

```
=ISLOGICAL(TRUE)   → TRUE
=ISLOGICAL(1)      → FALSE
```

---

### ISERROR

```
ISERROR(value)
```

Returns TRUE if the value is any error value (`#DIV/0!`, `#N/A`, `#REF!`, etc.).

```
=ISERROR(A1/B1)   → TRUE when B1 is 0
```

---

### XOR

```
XOR(logical1, [logical2, ...])
```

Returns TRUE if an odd number of the arguments are TRUE. Returns FALSE if all are the same.

```
=XOR(TRUE, FALSE)   → TRUE
=XOR(TRUE, TRUE)    → FALSE
=XOR(FALSE, FALSE)  → FALSE
```

---

### IFS

```
IFS(condition1, value1, [condition2, value2, ...])
```

Checks a series of conditions and returns the value corresponding to the first TRUE condition. Returns `#N/A` if no condition is TRUE.

```
=IFS(A1>90, "A", A1>80, "B", A1>70, "C")
```

---

### SWITCH

```
SWITCH(expression, value1, result1, [value2, result2, ...], [default])
```

Evaluates an expression against a list of values and returns the result for the first match. If no match, returns the optional default or `#N/A`.

```
=SWITCH(A1, 1, "Mon", 2, "Tue", 3, "Wed", "Other")
```

---

### IFNA

```
IFNA(value, value_if_na)
```

Returns `value_if_na` only when `value` is a `#N/A` error. Other error types pass through unchanged.

```
=IFNA(VLOOKUP(A1, B:C, 2, 0), "Not found")
=IFNA(#N/A, "missing")   → "missing"
=IFNA(#DIV/0!, "x")      → #DIV/0!  (not NA, passes through)
```

---

## Text

### LEFT

```
LEFT(text, [num_chars])
```

Returns the leftmost `num_chars` characters. Defaults to 1 if omitted.

```
=LEFT("Hello", 3)   → "Hel"
=LEFT("Hi")         → "H"
```

---

### RIGHT

```
RIGHT(text, [num_chars])
```

Returns the rightmost `num_chars` characters. Defaults to 1 if omitted.

```
=RIGHT("Hello", 3)  → "llo"
=RIGHT("Hi")        → "i"
```

---

### MID

```
MID(text, start_num, num_chars)
```

Returns `num_chars` characters from `text` starting at position `start_num` (1-based).

```
=MID("Hello World", 7, 5)  → "World"
=MID("abcdef", 2, 3)       → "bcd"
```

---

### LEN

```
LEN(text)
```

Returns the number of characters in a text string.

```
=LEN("Hello")   → 5
=LEN("")         → 0
```

---

### TRIM

```
TRIM(text)
```

Removes leading and trailing whitespace from text.

```
=TRIM("  hello  ")  → "hello"
```

---

### CONCATENATE

```
CONCATENATE(text1, [text2, ...])
```

Joins two or more text strings into one. Numbers and booleans are coerced to text.

```
=CONCATENATE("Hello", " ", "World")  → "Hello World"
=CONCATENATE(A1, " - ", B1)
```

---

### UPPER

```
UPPER(text)
```

Converts all characters to uppercase.

```
=UPPER("hello")  → "HELLO"
```

---

### LOWER

```
LOWER(text)
```

Converts all characters to lowercase.

```
=LOWER("HELLO")  → "hello"
```

---

### REPT

```
REPT(text, number_times)
```

Repeats text a given number of times. Returns empty string if `number_times` is 0 or negative.

```
=REPT("ab", 3)   → "ababab"
=REPT("-", 10)   → "----------"
```

---

### FIND

```
FIND(find_text, within_text, [start_num])
```

Returns the position (1-based) of the first occurrence of `find_text` inside `within_text`. Case-sensitive. Returns `#VALUE!` if not found.

```
=FIND("o", "Hello World")     → 5
=FIND("o", "Hello World", 6)  → 8  (search from position 6)
=FIND("x", "Hello")           → #VALUE!
```

---

### SUBSTITUTE

```
SUBSTITUTE(text, old_text, new_text, [instance_num])
```

Replaces occurrences of `old_text` with `new_text`. If `instance_num` is provided, only that specific occurrence is replaced. Case-sensitive.

```
=SUBSTITUTE("aabbaa", "a", "x")     → "xxbbxx"
=SUBSTITUTE("aabbaa", "a", "x", 2)  → "axbbaa"
```

---

### EXACT

```
EXACT(text1, text2)
```

Returns TRUE if two text strings are identical (case-sensitive). Returns FALSE otherwise.

```
=EXACT("abc", "abc")  → TRUE
=EXACT("ABC", "abc")  → FALSE
```

---

### SEARCH

```
SEARCH(find_text, within_text, [start_num])
```

Returns the position of `find_text` within `within_text`. Case-insensitive. Returns `#VALUE!` if not found.

```
=SEARCH("o", "Hello World")   → 5
=SEARCH("O", "Hello World")   → 5  (case-insensitive)
=SEARCH("x", "Hello")         → #VALUE!
```

---

### REPLACE

```
REPLACE(old_text, start_num, num_chars, new_text)
```

Replaces part of a text string with a different text string, based on position and length.

```
=REPLACE("abcdef", 2, 3, "XY")  → "aXYef"
=REPLACE("Hello", 1, 1, "J")    → "Jello"
```

---

### CHAR

```
CHAR(number)
```

Returns the character corresponding to the given ASCII/Unicode code point.

```
=CHAR(65)  → "A"
=CHAR(97)  → "a"
```

---

### CODE

```
CODE(text)
```

Returns the numeric code of the first character in a text string.

```
=CODE("A")     → 65
=CODE("abc")   → 97  (code of "a")
```

---

### VALUE

```
VALUE(text)
```

Converts a text string that represents a number to a number. Returns `#VALUE!` if the text cannot be parsed.

```
=VALUE("42")    → 42
=VALUE("3.14")  → 3.14
=VALUE("abc")   → #VALUE!
```

---

### T

```
T(value)
```

Returns the value if it is text, or an empty string if it is not.

```
=T("hello")  → "hello"
=T(42)       → ""
=T(TRUE)     → ""
```

---

## Date & Time

Date values are stored as `DateValue(LocalDate)` and datetime values as `DateTimeValue(LocalDateTime)`.

### DATE

```
DATE(year, month, day)
```

Returns a date value for the given year, month, and day. Overflow months/days are carried forward.

```
=DATE(2024, 1, 15)   → 2024-01-15
=DATE(2024, 13, 1)   → 2025-01-01  (month overflow)
```

---

### TODAY

```
TODAY()
```

Returns the current date. No arguments.

```
=TODAY()   → current date (e.g., 2026-05-01)
```

---

### NOW

```
NOW()
```

Returns the current date and time. No arguments.

```
=NOW()   → current datetime (e.g., 2026-05-01T14:30:00)
```

---

### YEAR

```
YEAR(date)
```

Returns the four-digit year from a date value. Accepts `DateValue`, `DateTimeValue`, or a serial number.

```
=YEAR(DATE(2024, 6, 15))  → 2024
=YEAR(TODAY())             → current year
```

---

### MONTH

```
MONTH(date)
```

Returns the month (1–12) from a date value.

```
=MONTH(DATE(2024, 6, 15))  → 6
```

---

### DAY

```
DAY(date)
```

Returns the day of the month (1–31) from a date value.

```
=DAY(DATE(2024, 6, 15))  → 15
```

---

### HOUR

```
HOUR(datetime)
```

Returns the hour (0–23) from a datetime value.

```
=HOUR(NOW())   → current hour
```

---

### MINUTE

```
MINUTE(datetime)
```

Returns the minute (0–59) from a datetime value.

```
=MINUTE(NOW())  → current minute
```

---

### SECOND

```
SECOND(datetime)
```

Returns the second (0–59) from a datetime value.

```
=SECOND(NOW())  → current second
```

---

### WEEKDAY

```
WEEKDAY(date, [return_type])
```

Returns the day of the week as a number. `return_type` controls numbering:

| return_type | Numbering |
|-------------|-----------|
| 1 (default) | Sunday=1, Monday=2, ..., Saturday=7 |
| 2 | Monday=1, Tuesday=2, ..., Sunday=7 |
| 3 | Monday=0, Tuesday=1, ..., Sunday=6 |

```
=WEEKDAY(DATE(2024, 1, 1))    → 2 (Monday, return_type 1)
=WEEKDAY(DATE(2024, 1, 1), 2) → 1 (Monday, return_type 2)
```

---

### EDATE

```
EDATE(start_date, months)
```

Returns a date that is `months` months before or after `start_date`. Positive `months` adds months; negative subtracts.

```
=EDATE(DATE(2024, 1, 15), 2)   → 2024-03-15
=EDATE(DATE(2024, 1, 15), -1)  → 2023-12-15
```

---

### EOMONTH

```
EOMONTH(start_date, months)
```

Returns the last day of the month that is `months` months away from `start_date`. `months=0` returns the last day of the current month.

```
=EOMONTH(DATE(2024, 1, 10), 0)  → 2024-01-31
=EOMONTH(DATE(2024, 1, 10), 1)  → 2024-02-29 (leap year)
```

---

### DAYS

```
DAYS(end_date, start_date)
```

Returns the number of days between `end_date` and `start_date`. Negative if `end_date` is before `start_date`.

```
=DAYS(DATE(2024, 1, 10), DATE(2024, 1, 1))  → 9
=DAYS(DATE(2024, 1, 1), DATE(2024, 1, 10))  → -9
```

---

### TIME

```
TIME(hour, minute, second)
```

Returns a time value (stored as `DateTimeValue` with date 1899-12-31). Hours wrap at 24; minutes and seconds at 60.

```
=TIME(14, 30, 0)   → 14:30:00
=TIME(25, 0, 0)    → 01:00:00 (wraps)
```

---

### DATEDIF

```
DATEDIF(start_date, end_date, unit)
```

Returns the difference between `start_date` and `end_date` in the specified `unit`. Common units:

| unit | Meaning |
|------|---------|
| "Y" | Complete years |
| "M" | Complete months |
| "D" | Days |
| "YM" | Months, ignoring years |
| "YD" | Days, ignoring years |
| "MD" | Days, ignoring months and years |

```
=DATEDIF(DATE(2020, 1, 1), DATE(2024, 6, 15), "Y")  → 4
=DATEDIF(DATE(2020, 1, 1), DATE(2024, 6, 15), "M")  → 53
=DATEDIF(DATE(2020, 1, 1), DATE(2024, 6, 15), "D")  → 1622
```

---

### ISOWEEKNUM

```
ISOWEEKNUM(date)
```

Returns the ISO 8601 week number (1–53) for the given date. Week 1 is the first week with a Thursday.

```
=ISOWEEKNUM(DATE(2024, 1, 1))   → 1
=ISOWEEKNUM(DATE(2024, 12, 31)) → 1
```

---

## Lookup & Reference

### VLOOKUP

```
VLOOKUP(lookup_value, table_range, col_index, [exact_match])
```

Searches the first column of `table_range` for `lookup_value` and returns the value in `col_index` (1-based). `exact_match` defaults to TRUE. Returns `#N/A` if not found.

```
=VLOOKUP("Widget", A2:C10, 2)         → value from column 2
=VLOOKUP(42, A2:C10, 3, TRUE)         → exact match, column 3
```

---

### INDEX

```
INDEX(range, row_num, [col_num])
```

Returns the value at the intersection of `row_num` and `col_num` (1-based) within `range`. Returns `#REF!` if out of bounds.

```
=INDEX(A1:C3, 2, 3)   → value at row 2, col 3
=INDEX(A1:A5, 3)      → value at row 3 of single-column range
```

---

### MATCH

```
MATCH(lookup_value, lookup_range, [match_type])
```

Returns the relative position (1-based) of `lookup_value` in `lookup_range`. `match_type`:
- `0` — exact match
- `1` (default) — largest value ≤ lookup_value (range must be sorted ascending)
- `-1` — smallest value ≥ lookup_value (range must be sorted descending)

Returns `#N/A` if not found.

```
=MATCH("Widget", A1:A10, 0)   → position of exact match
=MATCH(50, A1:A10, 1)         → position of nearest value ≤ 50
```

---

### COUNTA

```
COUNTA(value1, [value2, ...])
```

Counts non-blank cells. Unlike `COUNT`, includes text and booleans.

```
=COUNTA(A1:A10)   → count of non-empty cells
```

---

### COUNTIF

```
COUNTIF(range, criteria)
```

Counts cells in `range` that meet `criteria`. Criteria can be a value (exact match) or a comparison string (`">5"`, `"<10"`).

```
=COUNTIF(A1:A10, ">5")       → count of cells > 5
=COUNTIF(A1:A10, "Widget")   → count of cells equal to "Widget"
```

---

### SUMIF

```
SUMIF(range, criteria, [sum_range])
```

Sums cells in `sum_range` where the corresponding cells in `range` meet `criteria`. If `sum_range` is omitted, sums `range` directly.

```
=SUMIF(A1:A10, ">0", B1:B10)   → sum B where A > 0
=SUMIF(A1:A10, "East")          → sum A where A = "East"
```

---

### AVERAGEIF

```
AVERAGEIF(range, criteria, [average_range])
```

Averages cells in `average_range` where corresponding cells in `range` meet `criteria`. Returns `#DIV/0!` if no cells match.

```
=AVERAGEIF(A1:A10, ">0", B1:B10)  → average B where A > 0
```

---

### HLOOKUP

```
HLOOKUP(lookup_value, table_range, row_index, [exact_match])
```

Searches the first row of `table_range` for `lookup_value` and returns the value in `row_index` (1-based). `exact_match` defaults to TRUE. Returns `#N/A` if not found.

```
=HLOOKUP("Sales", A1:D2, 2)        → value from row 2
=HLOOKUP("Q1", A1:D10, 3, FALSE)   → approximate match, row 3
```

---

### CHOOSE

```
CHOOSE(index_num, value1, [value2, ...])
```

Returns a value from a list based on `index_num` (1-based). Returns `#VALUE!` if index is out of range.

```
=CHOOSE(2, "a", "b", "c")    → "b"
=CHOOSE(1, 10, 20, 30)       → 10
```

---

### LARGE

```
LARGE(number1, [number2, ...], k)
```

Returns the k-th largest value from the numbers. `k` is the last argument. Returns `#NUM!` if k exceeds the count of values.

```
=LARGE(5, 3, 1, 4, 2, 1)     → 5 (1st largest)
=LARGE(5, 3, 1, 4, 2, 3)     → 3 (3rd largest)
```

---

### SMALL

```
SMALL(number1, [number2, ...], k)
```

Returns the k-th smallest value from the numbers. `k` is the last argument. Returns `#NUM!` if k exceeds the count of values.

```
=SMALL(5, 3, 1, 4, 2, 1)     → 1 (1st smallest)
=SMALL(5, 3, 1, 4, 2, 3)     → 3 (3rd smallest)
```

---

### RANK

```
RANK(number, number1, [number2, ...])
```

Returns the rank of `number` within the given list, in descending order (largest value = rank 1). Returns `#N/A` if the number is not found.

```
=RANK(3, 5, 3, 1)            → 2 (rank in descending order)
=RANK(5, 5, 3, 1)            → 1 (largest)
```

---

### MEDIAN

```
MEDIAN(number1, [number2, ...])
```

Returns the median (middle value) of the numbers. For even count, returns the average of the two middle values. Returns `#NUM!` if no numeric values.

```
=MEDIAN(1, 3, 2)             → 2
=MEDIAN(1, 2, 3, 4)          → 2.5
```

---

### MODE

```
MODE(number1, [number2, ...])
```

Returns the most frequently occurring value (mode). Returns `#N/A` if no mode exists.

```
=MODE(1, 2, 2, 3)            → 2 (appears twice)
```

---

### STDEV

```
STDEV(number1, [number2, ...])
```

Returns the sample standard deviation of the numbers. Requires at least 2 numeric values. Returns `#DIV/0!` if fewer than 2 values.

```
=STDEV(2, 4, 4, 4, 5, 5, 7, 9)  → 2.138
```

---

### VAR

```
VAR(number1, [number2, ...])
```

Returns the sample variance of the numbers. Requires at least 2 numeric values. Returns `#DIV/0!` if fewer than 2 values.

```
=VAR(2, 4, 4, 4, 5, 5, 7, 9)    → 4.571
```

---

### COUNTIFS

```
COUNTIFS(range1, criteria1, [range2, criteria2, ...])
```

Counts cells that meet multiple criteria across multiple ranges. This is a multi-criteria version of COUNTIF. Currently aliases COUNTIF behavior.

```
=COUNTIFS(A1:A10, ">5", B1:B10, "East")
```

---

### SUMIFS

```
SUMIFS(sum_range, range1, criteria1, [range2, criteria2, ...])
```

Sums cells that meet multiple criteria across multiple ranges. This is a multi-criteria version of SUMIF. Currently aliases SUMIF behavior.

```
=SUMIFS(C1:C10, A1:A10, ">0", B1:B10, "East")
```

---

### AVERAGEIFS

```
AVERAGEIFS(average_range, range1, criteria1, [range2, criteria2, ...])
```

Averages cells that meet multiple criteria across multiple ranges. This is a multi-criteria version of AVERAGEIF. Currently aliases AVERAGEIF behavior.

```
=AVERAGEIFS(C1:C10, A1:A10, ">0", B1:B10, "East")
```

---

## Array

### SEQUENCE

```
SEQUENCE(rows, [columns], [start], [step])
```

Returns a sequence of numbers in an array. Defaults: `columns=1`, `start=1`, `step=1`.

```
=SEQUENCE(3)        → {1;2;3}
=SEQUENCE(2, 3)     → {1,2,3;4,5,6}
=SEQUENCE(3, 1, 10, 5)  → {10;15;20}
=SUM(SEQUENCE(3, 3))    → 45
```

---

### UNIQUE

```
UNIQUE(array)
```

Returns unique values from a range or list, preserving first-occurrence order.

```
=UNIQUE(1, 2, 2, 3, 1)  → {1,2,3}
=SUM(UNIQUE(A1:A5))
```

---

### SORT

```
SORT(array, [sort_index], [sort_order])
```

Returns sorted values from a range or list. Currently supports ascending sort only.

```
=SORT(3, 1, 2)          → {1,2,3}
=SORT(5, 2, 8, 1)       → {1,2,5,8}
```

---

### Array Literals

Inline array constants using braces:

```
{1, 2, 3}           → row vector
{1; 2; 3}           → column vector
{1, 2; 3, 4}        → 2×2 matrix
```

- `,` separates columns (same row)
- `;` separates rows
- Expressions are allowed: `{1+1, 2*3}`

Array literals evaluate to `RangeValue` and work with any function that accepts ranges:

```
=SUM({1, 2, 3})     → 6
=AVERAGE({4; 8})    → 6
```

---

## Charts

Charts are attached to worksheets and written as part of the XLSX output.

```java
import io.excel4j.core.model.*;

Worksheet sheet = wb.sheet("Sales");

Chart chart = Chart.builder()
    .title("Q1 Revenue")
    .type(ChartType.BAR)
    .categories(CellRange.of("A2", "A5"))
    .addSeries("Revenue", CellRange.of("B2", "B5"))
    .position(5, 1, 20, 10)
    .build();

sheet.addChart(chart);
```

**Chart types:** `BAR`, `LINE`, `PIE`

**Position:** `position(fromRow, fromCol, toRow, toCol)` — cell coordinates for chart placement.

Multiple charts per sheet supported.

---

## Custom Functions

Register custom functions via `FunctionRegistry`:

```java
import io.excel4j.formula.FunctionRegistry;
import io.excel4j.formula.FormulaEvaluator;
import io.excel4j.core.model.NumberValue;

FunctionRegistry registry = new FunctionRegistry();

// Add a TAX function: TAX(amount, rate)
registry.register("TAX", (args, ctx) -> {
    double amount = ((NumberValue) args.get(0)).value();
    double rate   = ((NumberValue) args.get(1)).value();
    return new NumberValue(amount * rate);
});

FormulaEvaluator evaluator = new FormulaEvaluator(workbook, registry);
workbook.recalculate(evaluator::evaluate);
```

Custom functions are case-insensitive (registered name is uppercased automatically).

---

## Error Types

| Error | Meaning |
|-------|---------|
| `#DIV/0!` | Division by zero |
| `#VALUE!` | Wrong argument type |
| `#REF!` | Invalid cell reference |
| `#NAME?` | Unrecognised function or name |
| `#N/A` | Value not found (VLOOKUP, MATCH) |
| `#NULL!` | Intersection of non-intersecting ranges |
| `#NUM!` | Invalid numeric operation (e.g., SQRT of negative) |
| `#CIRCULAR_REF` | Circular reference detected |

Errors propagate through most functions — passing an error cell as an argument usually returns that same error. Use `IFERROR` to suppress.
