# EnumSet, EnumMap

Java5에서 도입된 `Enum`을 통해 우리는 다음과 같이 읽기 쉽고 컴파일 타임에 오류를 확인할 수 있게 되었다.
```java
public enum OrderStatus {
    PREPARING(0),
    SHIPPED(1),
    DELIVERING(2),
    DELIVERED(3);

    protected int number;

    OrderStatus(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
```

이번에는 기본적인 문법보다 이펙티브 자바에서 소개한 `EnumSet`에 더해, `EnumMap`까지 대해서 공부해 보고자 한다.

## EnumSet
`EnumSet`은 `Enum` 타입으로만 이루어진 `Set` 인터페이스의 구현체이다.<br>
`Enum`타입만을 사용할 때 `EnumSet`을 사용하는게 더 좋다. `HashSet`과 비교했을 때 `EnumSet`은 성능이 더 빠르다.
`HashSet`은 내부적으로 `HashMap`을 사용하는데, 넣고자 하는 값의 hashCode를 이용해서 인덱스를 얻고 버킷(배열)에는 더미 오브젝트를 담게 된다.

```java
import java.util.EnumSet;

public class HashSet<E> {
    // ...

    public boolean add(E e) {
        return map.put(e, PRESENT) == null;
    }
}
```

하지만 `EnumSet`은 배열로 값들을 표현하는 것이 아닌, 101011 과 같이 bit vector로 표현이 가능해 성능이 훨씬 빠르다.

## EnumMap
`HashMap`은 키 값을 해시연산을 통해 버킷의 인덱스를 얻게 된다. 그리고 이 버킷은 내부적으로 SeparateChaining 방식으로 구성되어 있어
같은 인덱스가 나올 경우 같은 버킷에 들어가게 된다. 하지만 `EnumMap`은 키로 사용할 값이 제한되어 있기 때문에 그 개수만큼 길이를 가진 배열을 생성하고
해당 인덱스에 값을 넣으면 되기 때문에 성능이 빠르다.