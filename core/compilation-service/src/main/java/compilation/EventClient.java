package compilation;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.interaction.api.EventApi;

@FeignClient(name="event-service")
public interface EventClient extends EventApi {
}
