     D A20_AR2         S              2  0 DIM(6)
     D A20_N60         S              6  0
     D A20_I           S              2  0
     D £DBG_Str        S             100          VARYING

     D* Z-SUB con interi ed elementi di array
     C                   EVAL      A20_N60=20
     C                   EVAL      A20_I=6
     C                   EVAL      A20_AR2(1)=0
     C                   EVAL      A20_AR2(2)=0
     C                   EVAL      A20_AR2(3)=0
     C                   EVAL      A20_AR2(4)=0
     C                   EVAL      A20_AR2(5)=0
     C                   EVAL      A20_AR2(6)=20
     C                   Z-SUB     A20_N60       A20_AR2(A20_I)
     C                   EVAL      £DBG_Str='Res('
     C                                    +%CHAR(A20_AR2(1))+', '
     C                                    +%CHAR(A20_AR2(2))+', '
     C                                    +%CHAR(A20_AR2(3))+', '
     C                                    +%CHAR(A20_AR2(4))+', '
     C                                    +%CHAR(A20_AR2(5))+', '
     C                                    +%CHAR(A20_AR2(6))+')'
     C     £DBG_Str      DSPLY
