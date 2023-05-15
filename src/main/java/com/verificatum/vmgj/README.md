# Verificatum Multiplicative Groups Library for Java (VMGJ) - slightly modified

## Description

This package contains modified classes from the [Verificatum Multiplicative Groups Library for Java](https://github.com/verificatum/verificatum-vmgj) project by Douglas Wikström.
A slight modification was made to the VMG class in the com.verificatum.vmgj package. 
This modification was necessary to ensure compatibility with systems that have access to the required native libraries, as well as those that do not.

The [Verificatum Multiplicative Groups Library for Java](https://github.com/verificatum/verificatum-vmgj) allows invoking the 
[GMP Modular Exponentiation Extension (GMPMEE)](https://github.com/verificatum/verificatum-gmpmee) for simultaneous or fixed-base modular exponentiation.

To take advantage of the performance improvements offered by this module, you will need to make the native library accessible to your system through the paths specified in the java.library.path property. 
While support for Linux-based systems should work seamlessly by following the instructions provided on the project page (https://github.com/verificatum/verificatum-vmgj), 
support for Windows-based systems requires additional steps. 
However, you can generate a valid DLL using MSYS2. 
It's important to note that on 64-bit Linux, pointers occupy 8 bytes and can be converted to long, while on 64-bit Windows, pointers also occupy 8 bytes but need to be converted to long long, as long only occupies 4 bytes.

## Attribution

The Verificatum Multiplicative Groups Library for Java (VMGJ) wraps the GMP modular exponentiation extension and is copyrighted by Douglas Wikström.
We would like to thank Douglas for his kind support.

  - [Verificatum Multiplicative Groups Library for Java](https://github.com/verificatum/verificatum-vmgj)
  - [GMP Modular Exponentiation Extension (GMPMEE)](https://github.com/verificatum/verificatum-gmpmee)