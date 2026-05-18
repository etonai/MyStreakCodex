# Bug Tracking

This document tracks known bugs, their status, and resolutions for the Piano Track Opus app.

## Status Legend
- 🐛 **Open** - Bug is confirmed and needs fixing
- 🔄 **In Progress** - Bug is being worked on
- 🔍 **Verifying** - Fix implemented, awaiting user verification
- ✅ **Fixed** - Bug has been resolved and verified
- ❌ **Closed** - Bug closed without fix (e.g., not reproducible, won't fix)

## Known Bugs


## Bug Report Template

When reporting new bugs, please use this template:

```markdown
### 🐛 [Bug Title]
**Status:** Open
**Date Reported:** YYYY-MM-DD
**Severity:** [Critical/High/Medium/Low]
**Reporter:** [Name/Username]

**Description:**
[Clear description of the bug]

**Steps to Reproduce:**
1. [Step 1]
2. [Step 2]
3. [Step 3]

**Expected Behavior:**
[What should happen]

**Actual Behavior:**
[What actually happens]

**Environment:**
- App Version: [e.g., 1.0.8]
- Android Version: [e.g., Android 13]
- Device: [e.g., Pixel 7]

**Additional Information:**
[Any other relevant details, screenshots, logs, etc.]
```

---

## Fixed Bugs Archive

This section contains bugs that have been resolved and can serve as reference for similar issues in the future.

### Text Normalization Issues
**Common Cause:** Unicode character variations (apostrophes, quotes, whitespace)
**Solution Pattern:** Implement comprehensive text normalization using Unicode escape sequences
**Prevention:** Always normalize user input at entry points (manual input, import, etc.)