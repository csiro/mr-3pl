dun202@arcadia:~$ cat p.s
        .arch armv7-a
        .eabi_attribute 28, 1
        .eabi_attribute 20, 1
        .eabi_attribute 21, 1
        .eabi_attribute 23, 3
        .eabi_attribute 24, 1
        .eabi_attribute 25, 1
        .eabi_attribute 26, 2
        .eabi_attribute 30, 6
        .eabi_attribute 34, 1
        .eabi_attribute 18, 4
        .file   "p.c"
        .text
        .align  2
        .syntax unified
        .arm
        .fpu vfp
        .type   fpga_burst_read, %function
fpga_burst_read:
        @ args = 0, pretend = 0, frame = 16
        @ frame_needed = 1, uses_anonymous_args = 0
        push    {r4, r5, r6, r7, r8, r9, fp, lr}
        add     fp, sp, #28
        sub     sp, sp, #16
        str     r0, [fp, #-32]
        str     r1, [fp, #-36]
        str     r2, [fp, #-40]
        ldr     r3, [fp, #-40]
        sub     r3, r3, #1
        cmp     r3, #7
        ldrls   pc, [pc, r3, asl #2]
        b       .L2
.L4:
        .word   .L3
        .word   .L5
        .word   .L6
        .word   .L7
        .word   .L8
        .word   .L9
        .word   .L10
        .word   .L11
.L3:
        ldr     r3, [fp, #-32]
        ldr     r2, [r3]
        ldr     r3, [fp, #-36]
        str     r2, [r3]
        b       .L12
.L5:
        ldr     r1, [fp, #-36]
        ldr     r0, [fp, #-32]
        .syntax divided
@ 30 "p.c" 1
        @ burst read 2 words using ldm/stm on r2-r3
        ldm     r0, {r2-r3}
        stm     r1, {r2-r3}
@ 0 "" 2
        .arm
        .syntax unified
        b       .L12
.L6:
        ldr     r1, [fp, #-36]
        ldr     r0, [fp, #-32]
        .syntax divided
@ 31 "p.c" 1
        @ burst read 3 words using ldm/stm on r2-r4
        ldm     r0, {r2-r4}
        stm     r1, {r2-r4}
@ 0 "" 2
        .arm
        .syntax unified
        b       .L12
.L7:
        ldr     r1, [fp, #-36]
        ldr     r0, [fp, #-32]
        .syntax divided
@ 32 "p.c" 1
        @ burst read 4 words using ldm/stm on r2-r5
        ldm     r0, {r2-r5}
        stm     r1, {r2-r5}
@ 0 "" 2
        .arm
        .syntax unified
        b       .L12
.L8:
        ldr     r1, [fp, #-36]
        ldr     r0, [fp, #-32]
        .syntax divided
@ 33 "p.c" 1
        @ burst read 5 words using ldm/stm on r2-r6
        ldm     r0, {r2-r6}
        stm     r1, {r2-r6}
@ 0 "" 2
        .arm
        .syntax unified
        b       .L12
.L9:
        ldr     r1, [fp, #-36]
        ldr     r0, [fp, #-32]
        .syntax divided
@ 34 "p.c" 1
        @ burst read 6 words using ldm/stm on r2-r7
        ldm     r0, {r2-r7}
        stm     r1, {r2-r7}
@ 0 "" 2
        .arm
        .syntax unified
        b       .L12
.L10:
        ldr     r1, [fp, #-36]
        ldr     r0, [fp, #-32]
        .syntax divided
@ 35 "p.c" 1
        @ burst read 7 words using ldm/stm on r2-r8
        ldm     r0, {r2-r8}
        stm     r1, {r2-r8}
@ 0 "" 2
        .arm
        .syntax unified
        b       .L12
.L11:
        ldr     r1, [fp, #-36]
        ldr     r0, [fp, #-32]
        .syntax divided
@ 36 "p.c" 1
        @ burst read 8 words using ldm/stm on r2-r9
        ldm     r0, {r2-r9}
        stm     r1, {r2-r9}
@ 0 "" 2
        .arm
        .syntax unified
        b       .L12
.L2:
        bl      abort
.L12:
        nop
        sub     sp, fp, #28
        @ sp needed
        pop     {r4, r5, r6, r7, r8, r9, fp, pc}
        .size   fpga_burst_read, .-fpga_burst_read
        .align  2
        .syntax unified
        .arm
        .fpu vfp
        .type   fpga_burst_write, %function
fpga_burst_write:
        @ args = 0, pretend = 0, frame = 16
        @ frame_needed = 1, uses_anonymous_args = 0
        push    {r4, r5, r6, r7, r8, r9, fp, lr}
        add     fp, sp, #28
        sub     sp, sp, #16
        str     r0, [fp, #-32]
        str     r1, [fp, #-36]
        str     r2, [fp, #-40]
        ldr     r3, [fp, #-40]
        sub     r3, r3, #1
        cmp     r3, #7
        ldrls   pc, [pc, r3, asl #2]
        b       .L14
.L16:
        .word   .L15
        .word   .L17
        .word   .L18
        .word   .L19
        .word   .L20
        .word   .L21
        .word   .L22
        .word   .L23
.L15:
        ldr     r3, [fp, #-36]
        ldr     r2, [r3]
        ldr     r3, [fp, #-32]
        str     r2, [r3]
        b       .L24
.L17:
        ldr     r1, [fp, #-36]
        ldr     r0, [fp, #-32]
        .syntax divided
@ 45 "p.c" 1
        @ burst write 2 words using ldm/stm on r2-r3
        ldm     r1, {r2-r3}
        stm     r0, {r2-r3}
@ 0 "" 2
        .arm
        .syntax unified
        b       .L24
.L18:
        ldr     r1, [fp, #-36]
        ldr     r0, [fp, #-32]
        .syntax divided
@ 46 "p.c" 1
        @ burst write 3 words using ldm/stm on r2-r4
        ldm     r1, {r2-r4}
        stm     r0, {r2-r4}
@ 0 "" 2
        .arm
        .syntax unified
        b       .L24
.L19:
        ldr     r1, [fp, #-36]
        ldr     r0, [fp, #-32]
        .syntax divided
@ 47 "p.c" 1
        @ burst write 4 words using ldm/stm on r2-r5
        ldm     r1, {r2-r5}
        stm     r0, {r2-r5}
@ 0 "" 2
        .arm
        .syntax unified
        b       .L24
.L20:
        ldr     r1, [fp, #-36]
        ldr     r0, [fp, #-32]
        .syntax divided
@ 48 "p.c" 1
        @ burst write 5 words using ldm/stm on r2-r6
        ldm     r1, {r2-r6}
        stm     r0, {r2-r6}
@ 0 "" 2
        .arm
        .syntax unified
        b       .L24
.L21:
        ldr     r1, [fp, #-36]
        ldr     r0, [fp, #-32]
        .syntax divided
@ 49 "p.c" 1
        @ burst write 6 words using ldm/stm on r2-r7
        ldm     r1, {r2-r7}
        stm     r0, {r2-r7}
@ 0 "" 2
        .arm
        .syntax unified
        b       .L24
.L22:
        ldr     r1, [fp, #-36]
        ldr     r0, [fp, #-32]
        .syntax divided
@ 50 "p.c" 1
        @ burst write 7 words using ldm/stm on r2-r8
        ldm     r1, {r2-r8}
        stm     r0, {r2-r8}
@ 0 "" 2
        .arm
        .syntax unified
        b       .L24
.L23:
        ldr     r1, [fp, #-36]
        ldr     r0, [fp, #-32]
        .syntax divided
@ 51 "p.c" 1
        @ burst write 8 words using ldm/stm on r2-r9
        ldm     r1, {r2-r9}
        stm     r0, {r2-r9}
@ 0 "" 2
        .arm
        .syntax unified
        b       .L24
.L14:
        bl      abort
.L24:
        nop
        sub     sp, fp, #28
        @ sp needed
        pop     {r4, r5, r6, r7, r8, r9, fp, pc}
        .size   fpga_burst_write, .-fpga_burst_write
        .align  2
        .global main
        .syntax unified
        .arm
        .fpu vfp
        .type   main, %function
main:
        @ args = 0, pretend = 0, frame = 8
        @ frame_needed = 1, uses_anonymous_args = 0
        push    {fp, lr}
        add     fp, sp, #4
        sub     sp, sp, #8
        sub     r1, fp, #12
        sub     r3, fp, #8
        mov     r2, #2
        mov     r0, r3
        bl      fpga_burst_read
        sub     r1, fp, #8
        sub     r3, fp, #12
        mov     r2, #2
        mov     r0, r3
        bl      fpga_burst_write
        mov     r3, #0
        mov     r0, r3
        sub     sp, fp, #4
        @ sp needed
        pop     {fp, pc}
        .size   main, .-main
        .ident  "GCC: (GNU) 6.2.0"
        .section        .note.GNU-stack,"",%progbits
