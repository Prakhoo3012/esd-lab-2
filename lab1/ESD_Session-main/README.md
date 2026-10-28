# ESD Lab 1 — Git Recovery & Continuation Guide

Use this guide if you are joining the lab after it has started, if you missed a step, or if you need to recover the project to a particular checkpoint.

## 1. Clone the Repository

Open a terminal and run:

```bash
git clone https://github.com/sivasomanath2502/ESD_Session.git
```

Move into the project:

```bash
cd ESD_Session
```

The repository contains checkpoints for each stage of the lab.

---

## 2. Check Available Checkpoints

Run:

```bash
git tag
```

You will see:

```text
lab1-start
lab1-db-config
lab1-entity
lab1-repository
lab1-get
lab1-get-by-id
lab1-post
lab1-put
lab1-complete
```

Each tag represents a working version of the project at that stage. Git tags are references to specific points in the repository history.

---

## 3. Recover to a Checkpoint

Choose the checkpoint from which you want to continue.

For example, if you want to continue from the `lab1-get` checkpoint:

```bash
git switch --detach lab1-get
```

This loads the project exactly as it was at that checkpoint.

### Checkpoint → What it contains

| Checkpoint | Continue from here |
|---|---|
| `lab1-start` | Beginning of the project |
| `lab1-db-config` | Database configuration completed |
| `lab1-entity` | Product entity completed |
| `lab1-repository` | Repository completed |
| `lab1-get` | Get-all API completed |
| `lab1-get-by-id` | Get-by-ID API completed |
| `lab1-post` | POST API completed |
| `lab1-put` | PUT API completed |
| `lab1-complete` | Complete Lab 1 application |

**Example:** If you missed the `GET /products/{id}` part, start from:

```bash
git switch --detach lab1-get
```

Then continue the lab from the next step.

---

## 4. Continue the Lab

After switching to the required checkpoint:

1. Open the project in IntelliJ IDEA.
2. Make sure the project is using **JDK 17**.
3. Configure your MySQL password in `application.properties`.
4. Start the application.
5. Continue coding from the checkpoint shown above.

Do not modify the checkpoint itself. Your changes are made locally as you continue the lab.

---

## 5. If You Are Joining the Live Session Late

If the instructor is currently at a particular checkpoint:

1. Find the checkpoint currently being used.
2. Switch to that checkpoint.

For example:

```bash
git switch --detach lab1-post
```

3. Open the project in IntelliJ.
4. Follow the instructor from that point onward.

You do **not** need to repeat the earlier coding steps.

---

## 6. If Something Goes Wrong

If your local changes are causing problems and you want to return to a clean checkpoint:

```bash
git status
```

If you do not need your local changes, you can switch to the required checkpoint again:

```bash
git switch --detach <checkpoint-name>
```

For example:

```bash
git switch --detach lab1-get-by-id
```

**Warning:** Do not use commands that discard your work unless you are sure you no longer need your local changes.

---

## Quick Reference

### Clone

```bash
git clone https://github.com/sivasomanath2502/ESD_Session.git
cd ESD_Session
```

### View checkpoints

```bash
git tag
```

### Continue from a checkpoint

```bash
git switch --detach <checkpoint-name>
```

### Example

```bash
git switch --detach lab1-post
```

That's all you need to recover and continue the lab.
