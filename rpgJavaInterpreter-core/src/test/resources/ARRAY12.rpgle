      * Test DIM(%BIF) when used inside ds field

     D A1              S            100    DIM(2)
     D                 DS
     D  A2                          100    DIM(%ELEM(A1))
     D MSG             S             12
     **-------------------------------------------------------------------
     C                   EVAL      A2(1)   = 'AA'
     C                   EVAL      A2(2)   = 'BB'
     **-------------------------------------------------------------------
     C                   DSPLY                   A2(1)
     C                   DSPLY                   A2(2)
     **-------------------------------------------------------------------
     C                   SETON                                          LR
