#ifndef SPICE_BRIDGE_H
#define SPICE_BRIDGE_H

#include <stdbool.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

/* Establish main + touch connections. Returns true on success. */
bool spice_native_connect(const char *host, int32_t port, const char *password);

/* Request on the main connection. Returns a NUL-terminated JSON string
 * (response or {"error": ...}) owned by the caller; free with
 * spice_native_free_string. */
char *spice_native_request(const char *module, const char *function, const char *params_json);

/* Request on the dedicated touch connection (falls back to main). */
char *spice_native_touch_request(const char *module, const char *function, const char *params_json);

/* Tear down both connections. */
void spice_native_disconnect(void);

/* Last connect() failure reason (empty string when there is no error);
 * owned by the caller, free with spice_native_free_string. */
char *spice_native_last_error(void);

/* Release a string returned by spice_native_request / spice_native_touch_request. */
void spice_native_free_string(char *s);

#ifdef __cplusplus
}
#endif

#endif /* SPICE_BRIDGE_H */
