int main() {
  int x = __VERIFIER_nondet_int();
  int y;

  if(x < 4096){
    if(x < 2048){
        if(x < 1024){
          if(x < 512){
            if(x > 256){
              y = 9;
            } else{
              if(x < 128){
                y = 7;
              } else {
                y = 8;
              }
            }
          } else {
            y = 10;
          }
        } else{
          y = 11;
        }
    } else{
      y = 12;
    }
  } else {
    y = 13;
  }
}
