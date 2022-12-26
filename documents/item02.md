# ITEM2 생성자에 매개변수가 많다면 빌더를 고려하라

객체를 생성할 때 많은 필드를 초기화해야 할 경우 빌더 패턴을 사용하는게 좋다.

필드를 초기화 할 때 개발자는 생성자, 정적 팩터리 메서드, 빌더 중 선택해 제공 할 수 있다. 

# 점층적 생성자 패턴

먼저 생성자를 통해 필드를 초기화할 경우 점층적 생성자 패턴을 사용할 수 있다.

```java
public class NutritionFacts {
		private final int servingSize;
		private final int servings;
		private final int calories;
		private final int fat;
		private final int sodium;
		private final int carbohydrate;

		public NutritionFacts(int servingSize, int servings) {
				this(servingSize, servings, 0);
		}

		public NutritionFacts(int servingSize, int servings, int calories) {
				this(servingSize, servings, calories, 0);
		}

		...
}
```

점층적 생성자 패턴이란, 위 코드와 같이 필수 매개변수만 받는 생성자, 필수 매개변수와 선택 매개변수 1개만 받는 생성자 등 매개변수를 1개씩 늘려가며 연쇄적으로 호출하는 방식이다.

다만 이와 같은 방식은 사용자가 원치 않는 매개변수를 포함하기 쉽고, **매개변수가 많아질수록 코드를 작성하거나 관리하기 어려워진다**.

## 생성자 체이닝

위 코드에서는 생성자에서 다른 생성자를 호출하고 있는데 이를 생성자 체이닝이라고 한다. this, super 키워드를 통해 부모 클래스의 생성자나 자신의 다른 생성자를 호출 할 수 있다.

# 자바빈즈 패턴

다음 대안인 자바빈즈 패턴은 매개변수 없는 생성자로 객체를 생성하고, 세터를 통해 객체를 초기화하는 방식이다.

```java
public class NutritionFacts {
		private final int servingSize  = -1; // 필수
		private final int servings     = -1; // 필수
		private final int calories     = 0;
		private final int fat          = 0;
		private final int sodium       = 0;
		private final int carbohydrate = 0;

		public NutritionFacts(int servingSize, int servings) {}

		// setter
		public void setServingSize(int val) {this.servingSize = val;}
		public void setServings(int val) {this.servings = val;}
		public void setCalories(int val) {this.calories = val;}

		...
}
```

자바빈즈 패턴으로 객체를 초기화할 경우 점층적 생성자 패턴 방식보다는 코드를 읽기 쉽고 매개변수가 추가되어도 세터만 추가하면 된다.
다만 자바빈즈는 **객체가 완전히 생성되기 전까지는 일관성이 무너진 상태**가 된다는 심각한 단점이 있다.

즉 객체를 생성할 때 모든 필수 매개변수 등이 초기화되었는지 코드상에서 검증하기 어려워졌다는 말이다. 이 경우 런타임시 오류가 발생 할 수 있으며 불변 클래스를 만들수도 없다. 

이 단점을 보완하고자 객체의 생성이 끝나면 수동으로 얼리고(freezing), 그 전에는 사용하지 못하게 하는 방법도 있지만, 다루기가 어렵고 freeze 메서드 호출을 컴파일러가 보증할 수 없어 마찬가지로 런타임 오류에 취약하다.

# 빌더 패턴

이와같은 단점들은 빌더 패턴을 사용하면 해결 할 수 있다.

```java
private final int servingSize;
		private final int servings;
		private final int calories;
		private final int fat;
		private final int sodium;
		private final int carbohydrate;

		public static class Builder {
				private final int servingSize; // 필수
				private final int servings;    // 필수
				private int calories     = 0;
				private int fat          = 0;
				private int sodium       = 0;
				private int carbohydrate = 0;

				public Builder(int servingSize, int servings) {
						this.servingSize = servingSize;
						this.servings = servings;
				}

				public Builder calories(int val) {
						calorie = val; 
						return this;
				}

				public Builder fat(int val) {
						fat = val; 
						return this;
				}

				public Builder sodium(int val) {
						sodium = val; 
						return this;
				}

				public Builder carbohydrate(int val) {
						carbohydrate = val; 
						return this;
				}

				public Builder build() {
						return new NutritionFacts(this);
				}
		}

		private NutritionFacts(Builder builder) {
				servingSize = builder.servingSize;
				servings = builder.servings;
				calories = builder.calories;
				fat = builder.fat;
				sodium = builder.sodium;
				carbohydrate = builder.carbohydrate;
		}
}
```

위 코드에서 세터 메서드는 빌더 자신을 반환하기 때문에 연쇄적으로 호출이 가능하다.

이 같은 방식을 플루언트 API(fluent API) 혹은 메서드 연쇄(method chaining)라고 한다.

```java
NutritionFacts cocaCola = new NutritionFacts.Builder(240, 8)
				.calories(100).sodium(35).carbohydrate(27).build();
```

메서드 연쇄를 통해 위와같이 객체의 생성과 매개변수의 초기화를 한번에 수행할 수 있다.

이런식으로 빌더 패턴을 사용하면 코드를 사용하기 쉬워지고, 읽기도 쉬워진다. 또한 잘못된 매개변수를 build 등에서 검사해 불변식을 보장 할 수도 있다. 잘못된 매개변수일 경우 IllegalArgumentException을 던져주자.

빌더 패턴은 또한 계층적으로 설계된 클래스와 함께 쓰기에도 좋다.

아래 코드는 build 메서드를 추상 클래스로 정의하고 이를 상속받는 하위 클래스를 정의하는 예시이다.

- Pizza
    
    ```java
    public abstract class Pizza {
        public enum Topping { HAM, MUSHROOM, ONION, PEPPER, SAUSAGE }
        final Set<Topping> toppings;
    
        abstract static class Builder<T extends Builder<T>> {
            EnumSet<Topping> toppings = EnumSet.noneOf(Topping.class);
            public T addTopping(Topping topping) {
                toppings.add(Objects.requireNonNull(topping));
                return self();
            }
    
            abstract Pizza build();
    				
    		// 하위 클래스는 이 메서드를 재정의해 
    		// this를 반환하게 해야함
            protected abstract T self();
        }
        
        Pizza(Builder<?> builder) {
            toppings = builder.toppings.clone();
        }
    }
    ```
    
- NyPizza
    
    ```java
    public class NyPizza extends Pizza {
        public enum Size { SMALL, MEDIUM, LARGE }
        private final Size size;
    
        public static class Builder extends Pizza.Builder<Builder> {
            private final Size size;
    
            public Builder(Size size) {
                this.size = Objects.requireNonNull(size);
            }
    
            @Override public NyPizza build() {
                return new NyPizza(this);
            }
    
            @Override protected Builder self() { return this; }
        }
    
        private NyPizza(Builder builder) {
            super(builder);
            size = builder.size;
        }
    
        @Override public String toString() {
            return toppings + "로 토핑한 뉴욕 피자";
        }
    }
    ```
    
- Calzone
    
    ```java
    public class Calzone extends Pizza {
        private final boolean sauceInside;
    
        public static class Builder extends Pizza.Builder<Builder> {
            private boolean sauceInside = false;
    
            public Builder sauceInside() {
                sauceInside = true;
                return this;
            }
    
            @Override public Calzone build() {
                return new Calzone(this);
            }
    
            @Override protected Builder self() { return this; }
        }
    
        private Calzone(Builder builder) {
            super(builder);
            sauceInside = builder.sauceInside;
        }
    
        @Override public String toString() {
            return String.format("%s로 토핑한 칼초네 피자 (소스는 %s에)",
                    toppings, sauceInside ? "안" : "바깥");
        }
    }
    ```
    

각 하위 클래스의 빌더인 NyPizza.Builder는 NyPizza를 반환하고, Calzone.Builder는 Calzone를 반환한다.

이처럼 상위 클래스가 정의한 타입이 아닌, 하위 타입을 반환하는 기능을 **공변 반환 타이핑(covariant return typing)**이라고 한다.

## 그 밖의 장점

빌더 패턴은 그 밖에도 여러 객체를 순회하면서 객체를 생성할 수도 있고, 매개 변수에 따라서 다른 객체를 만들어 줄 수도 있다. 또한 일련 번호와 같은 필드는 자동으로 채워줄 수도 있다. 

## 단점

다만 빌더 패턴은 객체 생성 전 빌더부터 생성을 해주어야 하는데 성능에 민감할 경우 이는 문제가 될 수 있다. 그렇기 때문에 책에서는 매개 변수가 4개 이상일 때 빌더를 추가하는 것을 권장한다고 한다.

다만 API는 시간이 지날수록 매개 변수가 늘어날 수 있기 때문에 요구사항을 잘 파악해서 매개 변수가 늘어날 경우 중간에 생성자에서 빌더로 전환하기 보다는 처음부터 빌더로 시작하는게 편이 좋다.

## @Builder

빌더 클래스를 따로 만들기 번거로울 땐 Lombok의 @Builder 어노테이션을 사용하면 클래스에 자동으로 빌더 패턴을 적용해준다.

```java
@Builder
public class Product {
		private String name;
		private int size;
}
```

# IllegalArgumentException

java.lang.IllegalArgumentException은 잘못된 인자(illegal)나 적절하지 못한(inappropriate) 인자를 메서드에 넘겨 주었을 때 던지는 예외이다.

## CheckedException

체크 예외는 예외 처리를 하지 않을 경우 컴파일 에러를 발생시키는 예외이다.

다음과 같은 예외가 체크 예외에 속한다.

- FileNotFoundException (존재하지 않는 파일)
- ClassNotFoundException (잘못된 클래스 이름)

## UnCheckedException

반대로 언체크 예외는 예외 처리를 강제하지 않지만 런타임 예외를 발생할 수 있는 예외를 말한다.

다음과 같은 예외가 언체크 예외에 속한다.

- ArrayIndexOutOfBoundsException (범위를 벗어남)
- NullPointerException (null 참조)

# Resources

[Effective JAVA 3/E](http://www.yes24.com/Product/Goods/65551284)

[IllegalArgumentException이란](https://docs.oracle.com/javase/8/docs/api/java/lang/IllegalArgumentException.html)

[CheckedException, UnCheckedException](https://devlog-wjdrbs96.tistory.com/351)