
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
  
  return discount;
}