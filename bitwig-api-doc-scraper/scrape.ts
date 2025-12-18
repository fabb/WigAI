import { mkdir, readFile, writeFile } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath, pathToFileURL } from 'node:url'
import type { ClassData, EnumData, Fetcher, InterfaceData } from 'javadocs-scraper'

class MyFetcher implements Fetcher {
    baseUrl: URL
    loadHtml: (html: string) => any

    constructor(baseUrl: string, loadHtml: (html: string) => any) {
        this.baseUrl = new URL(baseUrl)
        this.loadHtml = loadHtml
    }

    async fetchRoot() {
        return this.fetch('index.html')
    }

    async fetch(url: string) {
        try {
            const fullUrl = new URL(url, this.baseUrl)
            const data = await readFile(fullUrl, 'utf-8')
            return { $: this.loadHtml(data), fullUrl: fullUrl.href }
        } catch (error) {
            console.error(`Error fetching ${url}:`, error)
            throw error
        }
    }
}

type ScrapeArgs = {
    input: string
    outDir: string
    apiVersion: string
    combinedOutFile: string | null
    dryRun: boolean
}

const toFileUrlDir = (input: string): string => {
    const trimmed = input.trim()
    if (trimmed.startsWith('file://') || trimmed.startsWith('http://') || trimmed.startsWith('https://')) {
        return trimmed.endsWith('/') ? trimmed : `${trimmed}/`
    }

    const absolutePath = path.resolve(trimmed)
    const asUrl = pathToFileURL(absolutePath).href
    return asUrl.endsWith('/') ? asUrl : `${asUrl}/`
}

const parseArgs = (argv: string[]): ScrapeArgs => {
    const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')

    let input =
        'file:///Applications/Bitwig%20Studio.app/Contents/Resources/Documentation/control-surface/api/'
    let outDir = path.join(repoRoot, 'docs', 'reference', 'bitwig-api')
    let apiVersion = 'v19'
    let combinedOutFile: string | null = path.join(path.dirname(fileURLToPath(import.meta.url)), 'bitwig-api-documentation.md')
    let dryRun = false

    const getValue = (i: number) => {
        const value = argv[i + 1]
        if (!value || value.startsWith('--')) throw new Error(`Missing value for ${argv[i]}`)
        return value
    }

    for (let i = 0; i < argv.length; i++) {
        const arg = argv[i]
        if (arg === '--help' || arg === '-h') {
            console.log(
                [
                    'Usage: node --experimental-transform-types scrape.ts [options]',
                    '',
                    'Options:',
                    '  -i, --input <path|fileUrl>    Input Javadoc directory (default: Bitwig Studio app bundle)',
                    '  -o, --out <dir>               Output root dir (default: ../docs/reference/bitwig-api)',
                    '  -v, --api-version <name>      Version folder name (default: v19)',
                    '      --no-combined             Do not write combined Markdown file',
                    '      --combined-out <file>     Path for combined Markdown file',
                    '      --dry-run                 Parse and report, but do not write files',
                    '',
                ].join('\n'),
            )
            process.exit(0)
        }
        if (arg === '--input' || arg === '-i') input = getValue(i)
        if (arg === '--out' || arg === '-o') outDir = getValue(i)
        if (arg === '--api-version' || arg === '-v') apiVersion = getValue(i)
        if (arg === '--no-combined') combinedOutFile = null
        if (arg === '--combined-out') combinedOutFile = getValue(i)
        if (arg === '--dry-run') dryRun = true
    }

    return {
        input: toFileUrlDir(input),
        outDir: path.resolve(outDir),
        apiVersion,
        combinedOutFile: combinedOutFile ? path.resolve(combinedOutFile) : null,
        dryRun,
    }
}

const args = parseArgs(process.argv.slice(2))

const { load } = await import('cheerio')
const { Scraper } = await import('javadocs-scraper')

const myFetcher = new MyFetcher(args.input, load)
const scraper = Scraper.with({ fetcher: myFetcher })

const scrapedJavadocs = await scraper.scrape()

const convertClassToMarkdown = (classData: ClassData): string => {
    let markdown = `# ${classData.name}\n\n`
    markdown += `- Kind: ${[...classData.modifiers, 'class'].join(' ')}\n`
    markdown += `- Package: \`${classData.package.name}\`\n\n`

    if (classData.description?.text) {
        markdown += `${classData.description.text}\n\n`
    }

    if (classData.extends) {
        markdown += `**Extends:** ${classData.extends.qualifiedName}\n\n`
    }

    if (classData.implements.size > 0) {
        markdown += `**Implements:** ${[...classData.implements.values()].map(impl => `\`${impl.qualifiedName}\``).join(', ')}\n\n`
    }

    const methods = classData.methods
    markdown += convertMethodsToMarkdown(methods)

    return markdown
};

const convertInterfaceToMarkdown = ( interfaceData: InterfaceData): string => {
    let markdown = `# ${interfaceData.name}\n\n`
    markdown += `- Kind: interface\n`
    markdown += `- Package: \`${interfaceData.package.name}\`\n\n`

    if (interfaceData.description?.text) {
        markdown += `${interfaceData.description.text}\n\n`
    }

    if (interfaceData.extends.size > 0) {
        markdown += `**Extends:** ${[...interfaceData.extends.map(extend => `\`${extend.qualifiedName}\``)].join(', ')}\n\n`
    }

    const methods = interfaceData.methods
    markdown += convertMethodsToMarkdown(methods)

    return markdown
};

const convertEnumToMarkdown = ( enumData: EnumData): string => {
    let markdown = `# ${enumData.name}\n\n`
    markdown += `- Kind: enum\n`
    markdown += `- Package: \`${enumData.package.name}\`\n\n`

    if (enumData.description?.text) {
        markdown += `${enumData.description.text}\n\n`
    }

    const values = enumData.constants
    if (values.size > 0) {
        markdown += `#### Values\n\n`
        for (const value of values) {
            markdown += `- \`${value[0]}\``
            if (value[1].description?.text) {
                markdown += `: ${value[1].description.text}`
            }
            markdown += `\n`
        }
        markdown += `\n`
    }

    const methods = enumData.methods
    markdown += convertMethodsToMarkdown(methods)


    return markdown
};

const convertMethodsToMarkdown = (methods: Map<string, any>) => {
    let markdown = ''
    if (methods.size > 0) {
        markdown += `## Methods\n\n`
        for (const [methodName, method] of methods) {
            markdown += `### ${methodName}\n\n`

            if (method.description?.text) {
                markdown += `${method.description.text}\n\n`
            }

            if (method.parameters.size > 0) {
                markdown += `**Parameters:**\n`
                for (const param of method.parameters) {
                    markdown += `- \`${param[1].name}\` (type ${param[1].type})`
                    if (param[1].description?.text) {
                        markdown += `: ${param[1].description.text}`
                    }
                    markdown += `\n`
                }
                markdown += `\n`
            }

            if (method.returns) {
                markdown += `**Returns:** type ${method.returns.type}`
                if (method.returns.description?.text) {
                    markdown += ` - ${method.returns.description.text}`
                }
                markdown += `\n\n`
            }
        }
    }
    return markdown
};

const toSafePathSegment = (value: string): string => value.replace(/[^A-Za-z0-9._-]/g, '_')
const packageToDir = (packageName: string): string =>
    packageName
        .split('.')
        .filter(Boolean)
        .map(seg => toSafePathSegment(seg))
        .join(path.sep)

type PackageShard = {
    classes: ClassData[]
    interfaces: InterfaceData[]
    enums: EnumData[]
}

const packageShards = new Map<string, PackageShard>()

const ensureShard = (packageName: string): PackageShard => {
    const existing = packageShards.get(packageName)
    if (existing) return existing
    const created: PackageShard = { classes: [], interfaces: [], enums: [] }
    packageShards.set(packageName, created)
    return created
}

scrapedJavadocs.getClasses().forEach((classData) => {
    ensureShard(classData.package.name).classes.push(classData)
})
scrapedJavadocs.getInterfaces().forEach((interfaceData) => {
    ensureShard(interfaceData.package.name).interfaces.push(interfaceData)
})
scrapedJavadocs.getEnums().forEach((enumData) => {
    ensureShard(enumData.package.name).enums.push(enumData)
})

const outRoot = path.join(args.outDir, args.apiVersion)
if (!args.dryRun) await mkdir(outRoot, { recursive: true })

const symbolIndex: Record<string, { kind: 'class' | 'interface' | 'enum'; path: string }> = {}

const writeShard = async (packageName: string, kind: 'class' | 'interface' | 'enum', typeName: string, content: string) => {
    const packageDir = path.join(outRoot, packageToDir(packageName))
    const fileName = `${toSafePathSegment(typeName)}.md`
    const filePath = path.join(packageDir, fileName)

    if (!args.dryRun) {
        await mkdir(path.dirname(filePath), { recursive: true })
        await writeFile(filePath, content, 'utf-8')
    }

    const qualifiedName = `${packageName}.${typeName}`
    const relativePath = path.relative(outRoot, filePath).split(path.sep).join('/')
    symbolIndex[qualifiedName] = { kind, path: relativePath }
}

// Write per-type shards
for (const [packageName, shard] of [...packageShards.entries()].sort(([a], [b]) => a.localeCompare(b))) {
    shard.classes.sort((a, b) => a.name.localeCompare(b.name))
    shard.interfaces.sort((a, b) => a.name.localeCompare(b.name))
    shard.enums.sort((a, b) => a.name.localeCompare(b.name))

    for (const cls of shard.classes) await writeShard(packageName, 'class', cls.name, convertClassToMarkdown(cls))
    for (const iface of shard.interfaces) await writeShard(packageName, 'interface', iface.name, convertInterfaceToMarkdown(iface))
    for (const enm of shard.enums) await writeShard(packageName, 'enum', enm.name, convertEnumToMarkdown(enm))
}

// Write index + symbol map
let indexOutput = `# Bitwig Control Surface API (${args.apiVersion})\n\n`
indexOutput += `Generated from local Javadoc HTML.\n\n`
indexOutput += `## Packages\n\n`

for (const [packageName, shard] of [...packageShards.entries()].sort(([a], [b]) => a.localeCompare(b))) {
    const total = shard.classes.length + shard.interfaces.length + shard.enums.length
    indexOutput += `### \`${packageName}\` (${total})\n\n`

    if (shard.classes.length) {
        indexOutput += `#### Classes\n\n`
        for (const cls of shard.classes) {
            const rel = symbolIndex[`${packageName}.${cls.name}`]?.path ?? ''
            indexOutput += `- [\`${cls.name}\`](${rel})\n`
        }
        indexOutput += `\n`
    }

    if (shard.interfaces.length) {
        indexOutput += `#### Interfaces\n\n`
        for (const iface of shard.interfaces) {
            const rel = symbolIndex[`${packageName}.${iface.name}`]?.path ?? ''
            indexOutput += `- [\`${iface.name}\`](${rel})\n`
        }
        indexOutput += `\n`
    }

    if (shard.enums.length) {
        indexOutput += `#### Enums\n\n`
        for (const enm of shard.enums) {
            const rel = symbolIndex[`${packageName}.${enm.name}`]?.path ?? ''
            indexOutput += `- [\`${enm.name}\`](${rel})\n`
        }
        indexOutput += `\n`
    }
}

if (!args.dryRun) {
    await writeFile(path.join(outRoot, 'index.md'), indexOutput, 'utf-8')
    await writeFile(path.join(outRoot, 'symbols.json'), JSON.stringify(symbolIndex, null, 2), 'utf-8')
}

// Optional: also write a single combined Markdown file for compatibility.
if (args.combinedOutFile) {
    let markdownOutput = `# Bitwig API Documentation\n\n`

    for (const [packageName] of scrapedJavadocs.getPackages()) {
        markdownOutput += `## Package: ${packageName}\n\n`

        // INFO somehow the packages in scrapedJavadocs.getPackages() do not contain the classes, interfaces and enums, so we have to iterate over them globally

        scrapedJavadocs.getClasses().forEach((classData) => {
            if (classData.package.name === packageName) {
                markdownOutput += `\n---\n\n`
                markdownOutput += convertClassToMarkdown(classData)
            }
        })

        scrapedJavadocs.getInterfaces().forEach((interfaceData) => {
            if (interfaceData.package.name === packageName) {
                markdownOutput += `\n---\n\n`
                markdownOutput += convertInterfaceToMarkdown(interfaceData)
            }
        })

        scrapedJavadocs.getEnums().forEach((enumData) => {
            if (enumData.package.name === packageName) {
                markdownOutput += `\n---\n\n`
                markdownOutput += convertEnumToMarkdown(enumData)
            }
        })
    }

    if (!args.dryRun) await writeFile(args.combinedOutFile, markdownOutput, 'utf-8')
}
if (args.dryRun) {
    const totalPackages = packageShards.size
    const totalSymbols = Object.keys(symbolIndex).length
    console.log(`Dry run complete.`)
    console.log(`Packages: ${totalPackages}`)
    console.log(`Symbols: ${totalSymbols}`)
    console.log(`Would write sharded docs to: ${outRoot}`)
    if (args.combinedOutFile) console.log(`Would write combined doc to: ${args.combinedOutFile}`)
} else {
    console.log(`Sharded docs written to: ${outRoot}`)
    if (args.combinedOutFile) console.log(`Combined doc written to: ${args.combinedOutFile}`)
}
