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

1. 需要让列名类继承Column类（如[Testcase](https://github.com/Wwqf/FormatOutput/blob/master/src/fout/custom/Testcase.java#L7)那样），为其中的每个属性都设置@FormatOp注解即可 (包括列名，重力方向，颜色)。
2. 然后将列名类以Class对象的方式传给FOut的[构造方法](https://github.com/Wwqf/FormatOutput/blob/master/src/fout/Fout.java#L64)。

### FOut类
1. add(Object...) 传入任意个参数，一个一个放入到表格中，一行结束则换行添加。
2. addln(Object...) 传入任意个参数，另起一行添加数据，如果形参的个数超过列数，则输出一条警告，并且将超过的数据移除（不再添加到表格中）。
3. skipPlace() 跳过一个位置，此位置的数据为null，（请查看输出样例）。
4. setColumnInvisible(int...) 传入任意个参数，这些参数为列的下标，调用此方法传入的实参列将不再显示。
5. resetVisible() 重置显示状态，即全部列都显示。

![类图](https://github.com/Wwqf/FormatOutput/blob/master/images/5.png)

## Java输出格式化 Version1.10
> 更新列表
> > 1. 增加表名，默认没有表名;
> > 2. 新增两种构建表格的方法，不再只有Class对象构建; 
> > 3. 新增方法 addColumn(index) 可以添加列项;
> > 4. 新增方法 addSubColumn() 为某一列构建子列项, 子列项的数量必须大于2;
> > 5. 新增方法 insertSubColumn() 可以指定一块子项区域添加数据
> > 6. 新增 可以控制列项的输出前景色和背景色;
> > 7. 新增 查找函数, 匹配成功的单元在输出时，会用特别的颜色标明.
> > 8. 去除 version1.0中的visible属性
> > 9. 更新方法名称


#### 一、调用方式
>Fout的构造函数
![Fout的构造函数](https://img-blog.csdnimg.cn/20200227193942395.png#pic_center)

> 可以构造一个含有多级子列项的表。（无奈, 真是很费脑）

1.如果你的列项是固定的（即有固定的列Column），那么可以通过Class对象获取注解的方式来生成一个表；提供两种注解**FormatOp**与**FormatOPSubColumn**。如果你使用这种方法，则需自定义类来使用注解，该类必须**继承FoutColumn**类。
>FormatOP可以设置该列的列名、方向（居左，居中，居右）、前景色、背景色，如Testcase类中State属性。
>FormpatOpSubColumn可以指定该列的子列项，正如Testcase类中Action和Goto属性一样，需要传入子列项的Class对象。

2.如果你的列项不是固定的（即不知道会有多少列），那么可以通过构造函数`Fout(ColumnAttr...)`来创建列项和子列项。
>在ColumnAttr中有快速创建一组列的方法qCreate(String...), 只需要传入列名即可。

3.如果你的列项不是固定的（即不知道会有多少列），也不想有类名，那么可以通过构造函数`Fout(int)`快速创建。



#### 二、方法列表
1. insert() 逐个插入数据。
2. insertln() 逐行插入数据。
3. skipPlace() 跳过一个位置(即一个单元格)。
4. insertSubColumn() 指定一个子列范围，逐个插入数据。
5. insertlnSubColumn() 指定一个子列范围，逐行插入数据。
6. setTableName() 设置表格名称。
7. findValue() 找到某个值, 在输出时，前景色是红色。
8. changeColumnColor() 改变某一列的前景色和背景色, 如果为0, 则不修改。
>![code3](https://img-blog.csdnimg.cn/20200227200939910.png#pic_center)
![result2](https://img-blog.csdnimg.cn/20200227201025527.png?#pic_center)
