v1.2.2

### ✨ New Features

- **Added Ignite Events**
    - Implemented `PortalIgniteEvent` and `PortalPreIgniteEvent` interfaces.
    - Included corresponding handlers and invokers.
    - 🔧 These events allow:
        - Executing custom logic *before* and *after* portal ignition.
        - Cancelling the ignition entirely, if desired.

- **Player Entity in Ignite Context**
    - The player is now included as part of the **ignition source** in item-use events.
    - ✅ Matches upstream behavior.
    - 🔍 Enables event consumers to identify which player attempted to light the portal.

---

### ⚠️ Breaking Change

- **Fixed Item Usage Behavior on Failed Portal Lighting**
    - `customportalapi-reforged` previously cancelled item usage **when ignition failed**, which is **opposite** of upstream behavior.
    - This has been reversed: **item usage is now only cancelled if the portal is successfully lit**.
    - Example: Using an **Eye of Ender** to light a portal will no longer cause it to fly off and possibly break—it will be consumed *only* when ignition succeeds.

---

Credit murderspagurder for this PR!