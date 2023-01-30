# ITEM3 private 생성자나 열거 타입으로 싱글턴임을 보증하라

싱글턴(singleton)은 인스턴스를 오직 하나만 생성할 수 있는 클래스를 말한다.

싱글턴은 인스턴스가 하나임이 보장되기 때문에 메모리 낭비를 방지할 수 있지만 mock 구현으로 대체 할 수 없기 때문에 이를 사용하는 클라이언트를 테스트하기 쉽지 않다는 단점도 존재한다.

싱글턴을 만드는 방식으로는 크게 private 생성자를 이용하는 방식이나 열거 타입을 사용하는 방식이 있다.

## private 생성자 방식

우선 생성자를 private로 감추고 publid static 필드를 사용한다.

```java
public class Elvis {
		public static final Elvis INSTANCE = new Elvis();
		private Elvis() { ... }

		public void leaveTheVuilding() { ... }
}
```

private 생성자는 INSTANCE 필드가 초기화 될 때 한번만 실행되고, 외부에 노출된 다른 생성자가 없기 때문에 싱글턴임이 보장된다. 

다만 권한이 있는 클라이언트는 리플렉션 API (AccessibleObject.setAccessible)를 사용해 private를 사용할 수 있다. 이 경우 두번째 객체가 생성 될 때 예외를 발생시키면 된다.

<aside>
✅ private 메서드를 테스트하고 싶을 경우 리플렉션 API를 사용할 수 있다.

```java
public class Test {
    private void hello() {
        System.out.println("Hello");
    }
}
```

```java
Method method = Test.class.getDeclaredMethod("hello");
method.setAccessible(true);
method.invoke();
```

setAccessible를 통해 접근을 허용해주고 invoke를 사용해 메서드를 실행한다.

</aside>

또 다른 방식으론 정적 팩터리 메서드를 사용한다.

```java
public class Elvis {
		private static final Elvis INSTANCE = new Elvis();
		private Elvis() { ... }
		public static Elvis getInstance() { return INSTANCE; }
		
		public void leaveTheBuilding() { ... }
}
```

getInstance 메소드는 항상 INSTANCE를 반환하므로 싱글턴임이 보장된다. 

이 방식의 장점은 

- 간결하며 해당 클래스가 싱글턴임이 API에 바로 드러난다.
- 또한 원할경우 싱글턴이 아니게 변경할 수도 있다. 예를들어 호출하는 쓰레드별로 다른 인스턴스를 넘겨 줄 수 있다.
- 원한다면 정적 팩터리를 제네릭 싱글턴 팩터리로 만들 수도 있으며(아이템 30),
- 정적 팩터리의 메서드 참조를 공급자(supplier)로 사용할 수 있다. (Elvis::getInstance → Supplier<Elvis>로 사용)

### 직렬화

두 방식의 싱글턴 클래스 모두 직렬화시 단순 Serializable를 선언하는 것으로는 부족하다. 모든 인스턴스 필드를 일시적(Transient) 선언, readResolve 메서드를 제공해야 한다. (아이템 89)

이렇게 하지 않으면 역직렬화시 매번 새로운 인스턴스가 만들어진다.

```java
// 싱글턴임을 보장해주는 readResolve 메서드
private Object readResolve() {
		// '진짜' Elvis를 반환하고, 가짜 Elvis는 가비지 컬렉터에 맡긴다.
		return INSTANCE;
}
```

<aside>
✅ **자바 직렬화란?**

- 자바 시스템 내부에서 사용되는 객체 또는 데이터를 바이트 형태로 데이터 변환하는 기술
- 또는 바이트로 변환된 데이터를 다시 객체로 변환하는 기술 (역직렬화)

자바 직렬화 기본조건 : `java.io.Serializable` 인터페이스를 상속받은 객체

```java
Member member = new Member("김배민", "deliverykim@baemin.com", 25);
    byte[] serializedMember;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(member);
            // serializedMember -> 직렬화된 member 객체 
            serializedMember = baos.toByteArray();
        }
    }
    // 바이트 배열로 생성된 직렬화 데이터를 base64로 변환
    System.out.println(Base64.getEncoder().encodeToString(serializedMember));
}
```

기본조건에 해당한다면 `java.io.ObjectOutputStream` 객체를 사용해 직렬화를 수행한다.

자바 역직렬화 조건

- 클래스 패스에 존재하며 import 되어 있어야 함
- 직렬화 대상 객체는 동일한 serialVersionUID를 가지고 있어야 함

```java
// 직렬화 예제에서 생성된 base64 데이터 
  String base64Member = "...생략";
  byte[] serializedMember = Base64.getDecoder().decode(base64Member);
  try (ByteArrayInputStream bais = new ByteArrayInputStream(serializedMember)) {
      try (ObjectInputStream ois = new ObjectInputStream(bais)) {
          // 역직렬화된 Member 객체를 읽어온다.
          Object objectMember = ois.readObject();
          Member member = (Member) objectMember;
          System.out.println(member);
      }
  }
```

**직렬화 할 때 주의할점은 직렬화와 역직렬화를 진행하는 시스템이 서로 다를 수 있다는 점이다.**

</aside>

## 열거 타입 방식

```java
public enum Elvis {
		INSTANCE;
		
		public void leaveTheBuilding() { ... }
}
```

열거 타입을 사용해 더 간결하고 추가 노력없이 직렬화가 가능하며, 아주 복잡한 직렬화 상황이나 리플렉션 시 추가 인스턴스가 생기는 것을 방지할 수 있다. 

단, 만들려는 싱글턴이 Enum외의 클래스를 상속해야하는 경우 이 방식은 사용할 수 없다.

# Resources

[Effective JAVA 3/E](http://www.yes24.com/Product/Goods/65551284)