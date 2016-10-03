package uk.co.beerdragon.test;

public class A {

  public static void foo () {
    System.out.println ("Class A");
  }

  public static void bar () {
    System.err.println ("Class A");
    B.foo ();
  }

}