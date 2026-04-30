package com.github.alantr7.codebots.cbslang.low;

public class ByteCode {

    public static final byte DEFC           = 1;
    public static final byte IMPF           = 2;
    public static final byte DEFL           = 3;
    public static final byte MOV            = 4;
    public static final byte PUSH           = 5;
    public static final byte POP_VOID       = 6;
    public static final byte POP            = 7;
    public static final byte CMP            = 8;
    public static final byte JMP            = 9;
    public static final byte JE             = 10;
    public static final byte JNE            = 11;
    public static final byte JL             = 12;
    public static final byte JLE            = 13;
    public static final byte JG             = 14;
    public static final byte JGE            = 15;
    public static final byte CALL           = 16;
    public static final byte RET            = 17;
    public static final byte ADD            = 18;
    public static final byte SUB            = 19;
    public static final byte MUL            = 20;
    public static final byte DIV            = 21;
    public static final byte MOD            = 22;
    public static final byte CFLTI          = 23;
    public static final byte CIFLT          = 24;
    public static final byte CAT            = 25;
    public static final byte HALT           = 26;
    public static final byte EXIT           = 127;

    public static final byte DEFC_INT       = 1;
    public static final byte DEFC_FLT       = 2;
    public static final byte DEFC_STR       = 3;

    public static final byte ACC_DIR        = 1;
    public static final byte ACC_MEM        = 2;
    public static final byte ACC_MEM_OFF    = 3;

    public static final byte REF_CONST      = 1;
    public static final byte REF_REG        = 2;

    public static final byte REG_EIP        = 1;
    public static final byte REG_EBP        = 2;
    public static final byte REG_ESP        = 3;
    public static final byte REG_EAX        = 4;
    public static final byte REG_EBX        = 5;
    public static final byte REG_CMP        = 6;
    public static final byte REG_RAX        = 7;

}
