
**Control flow graph for C programs using ANTLR**

I used [`antlr\grammars-v4`](https://github.com/antlr/grammars-v4/blob/master/c/C.g4) project for build AST. \
Please note that I have not used this code in its purest form.

For build ANTLR parser I used [IntelliJ IDEA plugin](https://plugins.jetbrains.com/plugin/7358-antlr-v4-grammar-plugin). \
Just do right click on `java/ru/er_log/antlr/C.g4` source page and then click `Generate ANTLR recognizer`.

<p align="center">
    <img width="400px" alt="Graph preview" src="preview.png" />
</p>

This graph shows the result for this `C` code:

```C
int main()
{
    call1();
label:
    call2();

    for (int i = 0; i < 10; i++) {
        if (1 > 0) {
            if (if_check_1) printif();
            test1();

            break; // Внимание! Тут BREAK.

            if (if_check_2) printif();
            else test2();
        }
        // This is comment line.
        else if (true && false) {
            for(;;) {
                callInsideFor();
            }

            do {
                callInsideDoWhile();

                continue; // Внимание! Тут CONTINUE.

                deadCallFromDoWhile();
            } while(10 > 5); /* This is comment. */

            while(while_check_1) {
                do {
                    callInsideDoubleDoWhile();
                } while(dowhile_check_1);
            }
        }
        else {
            goto label; // Внимание! Тут GOTO.

            deadLineAfterGOTO();
        }

        if (if_check_3) { return; }
    }

    call3();
}

int oneMoreFunc() {
    doSomething();
}
```