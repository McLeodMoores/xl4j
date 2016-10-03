package uk.co.beerdragon.test;

public class B {

  public static void foo () {
    System.out.println ("Class B");
  }

  public static void bar () {
    System.err.println ("Class B");
    A.foo ();
  }

}