extern void __VERIFIER_error() __attribute__ ((__noreturn__));

int computeDiscount(unsigned int day, int isThursday)
{
  int discount;
  if (isThursday)
  {
    discount=5;
  }
  else
  {
    discount=day%7;
  }
  if(discount<0 || discount>7)
  {
    ERROR: __VERIFIER_error();
  }
  return discount;
}