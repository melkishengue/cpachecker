int find_lcm(int, int);   // function prototype declaration

int main(int a, int b)
{
    int lcm;
    lcm = find_lcm(a,b);
    return lcm;
}

int find_lcm(int a, int b) {
    static int temp = 1;    
    if (temp%a == 0 && temp%b == 0) {
        return temp;
    } else {
        temp++;
        find_lcm(a, b);
        return temp;
    }
}