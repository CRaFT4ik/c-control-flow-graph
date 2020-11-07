
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
    call2();
label:
    call3();

    if (true) { call1(); }
    do { call2(); } while (true);

    for(int i = 0; i < 10; i++) {
        if (1*0 > 0) {
            if (true) printif();
            test2();
            break;
            if (true) printif();
            else test2();
        }
        // Comment block
        else if (true && false) {
            for(;;) { doo(); }
            do { doo(); continue; lol(); } while(10 > 5); /* This is comment. */
            while(true == true) { do { doo(); } while(true); }
        }
        else {
            goto label;
            printif3();
        }

        if (true) { return; }
    }

    call5();
    call6();
}

int onemorefunc() {
    dodo();
}
```