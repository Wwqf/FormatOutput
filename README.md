# FormatOutput
Java 's format output (table style)

## Java输出格式化 Version1.0

### 调用样例1
![调用样例1](https://github.com/Wwqf/FormatOutput/blob/master/images/2.png)

### 输出样例1
![输出样例1](https://github.com/Wwqf/FormatOutput/blob/master/images/1.png)

### 调用样例2
![调用样例1](https://github.com/Wwqf/FormatOutput/blob/master/images/4.png)

### 输出样例2
![输出样例1](https://github.com/Wwqf/FormatOutput/blob/master/images/3.png)

1. 需要让列名类继承Column类（如[Testcase](https://github.com/Wwqf/FormatOutput/tree/master/src/Testcase.java#L6)那样），为其中的每个属性都设置@FormatOp注解即可 (包括列名，重力方向，颜色)。
2. 然后将列名类以Class对象的方式传给FOut的[构造方法](https://github.com/Wwqf/FormatOutput/blob/master/src/fout/FOut.java#L29)。

### FOut类
1. add(Object...) 传入任意个参数，一个一个放入到表格中，一行结束则换行添加。
2. addln(Object...) 传入任意个参数，另起一行添加数据，如果形参的个数超过列数，则输出一条警告，并且将超过的数据移除（不再添加到表格中）。
3. skipPlace() 跳过一个位置，此位置的数据为null，（请查看输出样例）。
4. setColumnInvisible(int...) 传入任意个参数，这些参数为列的下标，调用此方法传入的实参列将不再显示。
5. resetVisible() 重置显示状态，即全部列都显示。

![类图](https://github.com/Wwqf/FormatOutput/blob/master/images/5.png)
