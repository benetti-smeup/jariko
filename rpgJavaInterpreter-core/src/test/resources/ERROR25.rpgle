     V* ==============================================================
     D* 06/06/24
     D* Purpose: Must fire the following errors
     D* line 8 - Incorrect by empty string
     D* line y - Incorrect by wrong format date
     V* ==============================================================

     D DATE1           S               D   DATFMT(*JUL) INZ(D'1939-12-31')
     D DATE2           S               D   DATFMT(*JUL) INZ(D'2040-01-01')